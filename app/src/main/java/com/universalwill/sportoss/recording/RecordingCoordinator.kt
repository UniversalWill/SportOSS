package com.universalwill.sportoss.recording

import com.universalwill.sportoss.domain.enums.WorkoutType
import com.universalwill.sportoss.domain.recording.GpsQuality
import com.universalwill.sportoss.domain.recording.GpsSource
import com.universalwill.sportoss.domain.recording.GpsStatus
import com.universalwill.sportoss.domain.recording.RecordingClock
import com.universalwill.sportoss.domain.recording.RecordingControl
import com.universalwill.sportoss.domain.recording.RecordingEngine
import com.universalwill.sportoss.domain.recording.RecordingError
import com.universalwill.sportoss.domain.recording.RecordingPhase
import com.universalwill.sportoss.domain.recording.RecordingServiceLauncher
import com.universalwill.sportoss.domain.recording.RecordingSnapshot
import com.universalwill.sportoss.domain.recording.RecordingStore
import com.universalwill.sportoss.domain.recording.TrackPoint
import com.universalwill.sportoss.recording.di.RecordingScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class RecordingCoordinator @Inject constructor(
    private val store: RecordingStore,
    private val gps: GpsSource,
    private val clock: RecordingClock,
    private val launcher: RecordingServiceLauncher,
    @RecordingScope private val scope: CoroutineScope,
) : RecordingControl {
    private val mutableState = MutableStateFlow(RecordingSnapshot())
    override val state = mutableState.asStateFlow()
    private val mutex = Mutex()
    private val engine = RecordingEngine()
    private val pendingPoints = mutableListOf<TrackPoint>()
    private var uiVisible = false
    private var serviceAlive = false
    private var starting = false
    private var initialized = false
    private var error: RecordingError? = null

    init {
        retryLoad()
        scope.launch {
            gps.reading.collect { reading ->
                command {
                    if (reading.status != GpsStatus.Ready) engine.breakSegment()
                    if (serviceAlive && reading.status == GpsStatus.Ready) {
                        reading.fix?.let { fix ->
                            engine.accept(fix, clock.elapsedRealtimeMillis())?.let { point ->
                                pendingPoints += point
                                checkpoint()
                            }
                        }
                    }
                    publish()
                }
            }
        }
        scope.launch {
            while (isActive) {
                delay(1_000)
                command {
                    if (uiVisible || serviceAlive) gps.refresh()
                    if (serviceAlive && phase() == RecordingPhase.Recording) checkpoint()
                    publish()
                }
            }
        }
    }

    override fun retryLoad() = command {
        if (initialized) return@command
        store.loadSession()?.let(engine::restore)
        initialized = true
        error = null
        publish()
    }

    override fun setUiVisible(visible: Boolean) = command {
        uiVisible = visible
        updateGpsSubscription()
        publish()
    }

    override fun toggle(type: WorkoutType) {
        val requestedPhase = state.value.phase
        command {
            if (!initialized || starting || phase() != requestedPhase) return@command
            when (phase()) {
                RecordingPhase.Recording -> pauseInternal()
                RecordingPhase.Idle, RecordingPhase.Paused, RecordingPhase.Interrupted -> {
                    gps.refresh()
                    if (!isGpsReady()) {
                        publish()
                        return@command
                    }
                    starting = true
                    error = null
                    publish()
                    try {
                        launcher.launch(engine.snapshot(clock.elapsedRealtimeMillis())?.workoutType ?: type)
                    } catch (_: RuntimeException) {
                        starting = false
                        error = RecordingError.ServiceStart
                        publish()
                    }
                }
                else -> Unit
            }
        }
    }

    /** Called only after the service has entered the foreground. */
    fun activate(type: WorkoutType) = command {
        starting = false
        serviceAlive = true
        updateGpsSubscription()
        if (!initialized || !isGpsReady()) {
            publish()
            return@command
        }
        val now = clock.elapsedRealtimeMillis()
        when (phase()) {
            RecordingPhase.Idle -> engine.start(store.createSession(type, clock.epochMillis()), now)
            RecordingPhase.Paused, RecordingPhase.Interrupted -> engine.resume(now)
            else -> {
                publish()
                return@command
            }
        }
        checkNotNull(gps.reading.value.fix).let { fix ->
            engine.accept(fix, clock.elapsedRealtimeMillis())?.let { pendingPoints += it }
        }
        checkpoint()
        error = null
        publish()
    }

    fun pause() = command {
        if (phase() == RecordingPhase.Recording && !starting) pauseInternal()
    }

    private suspend fun pauseInternal() {
        engine.freeze(RecordingPhase.Paused, clock.elapsedRealtimeMillis())
        checkpoint()
        publish()
    }

    override fun finish() = command {
        if (!initialized || starting) return@command
        val current = engine.snapshot(clock.elapsedRealtimeMillis()) ?: return@command
        if (current.phase == RecordingPhase.Saving) return@command
        engine.freeze(RecordingPhase.Saving, clock.elapsedRealtimeMillis())
        error = null
        publish()
        try {
            // Persist stopped time before finalizing so recovery never resumes a failed save.
            checkpoint()
            store.finish(checkNotNull(engine.snapshot(clock.elapsedRealtimeMillis())), pendingPoints.toList())
            pendingPoints.clear()
            engine.clear()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            engine.freeze(RecordingPhase.SaveFailed, clock.elapsedRealtimeMillis())
            error = RecordingError.Save
            try {
                checkpoint()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // The in-memory stopped snapshot remains retryable when storage is unavailable.
            }
        }
        publish()
    }

    fun serviceFailed() = command {
        starting = false
        serviceAlive = false
        if (phase() == RecordingPhase.Recording) {
            engine.freeze(RecordingPhase.Interrupted, clock.elapsedRealtimeMillis())
            checkpoint()
        }
        error = RecordingError.ServiceStart
        updateGpsSubscription()
        publish()
    }

    fun serviceDetached() = command {
        serviceAlive = false
        starting = false
        if (phase() == RecordingPhase.Recording || phase() == RecordingPhase.Paused) {
            engine.freeze(RecordingPhase.Interrupted, clock.elapsedRealtimeMillis())
            checkpoint()
        }
        updateGpsSubscription()
        publish()
    }

    private fun isGpsReady(): Boolean = gps.reading.value.let {
        it.status == GpsStatus.Ready &&
            GpsQuality.status(it.fix, clock.elapsedRealtimeMillis()) == GpsStatus.Ready
    }

    private fun updateGpsSubscription() {
        if (uiVisible || serviceAlive) gps.start() else gps.stop()
    }

    private suspend fun checkpoint() {
        val snapshot = engine.snapshot(clock.elapsedRealtimeMillis()) ?: return
        store.checkpoint(snapshot, pendingPoints.toList())
        pendingPoints.clear()
    }

    private fun phase() = engine.snapshot(clock.elapsedRealtimeMillis())?.phase ?: RecordingPhase.Idle

    private fun publish() {
        val snapshot = engine.snapshot(clock.elapsedRealtimeMillis())
        mutableState.value = RecordingSnapshot(
            session = snapshot,
            phase = if (starting) RecordingPhase.Starting else snapshot?.phase ?: RecordingPhase.Idle,
            gps = gps.reading.value.status,
            isLoading = !initialized,
            error = error,
        )
    }

    private fun command(block: suspend () -> Unit) {
        scope.launch {
            mutex.withLock {
                try {
                    block()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    starting = false
                    engine.freeze(RecordingPhase.SaveFailed, clock.elapsedRealtimeMillis())
                    error = RecordingError.Storage
                    updateGpsSubscription()
                    publish()
                }
            }
        }
    }
}
