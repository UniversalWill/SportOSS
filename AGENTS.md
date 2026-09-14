## Project overview

SportOSS is an open-source, offline-first Android application for recording
and exploring sports activities.

The project is designed around local data ownership: core functionality and
activity recording must work without an internet connection, cloud service,
or remote account. A user profile, when needed, exists locally inside the
application.

SportOSS aims to provide a transparent and extensible alternative to
proprietary fitness ecosystems. The project favors open technologies,
privacy-conscious defaults, replaceable external providers, and
community-driven development.

The main stack is:

- Kotlin and Jetpack Compose
- Material 3
- Jetpack Navigation 3
- Hilt with KSP
- Coroutines and StateFlow
- MapLibre Compose
- Gradle version catalogs

## Product principles

- Offline-first is a core architectural requirement, not a fallback mode.
- Core activity recording must work without network access.
- Activity, location, and profile data are stored locally by default.
- The application must not require a remote account.
- A user profile, when required, is local to the application.
- Cloud synchronization must remain optional if introduced in the future.
- Network failures must not prevent users from recording or viewing locally
  available activities.
- External services must enhance the application rather than become
  dependencies of its core functionality.
- Prefer open technologies and replaceable providers.
- Avoid coupling the domain model to a map, analytics, storage, or
  synchronization vendor.
- Collect privacy-sensitive data only for explicit user-facing functionality.
- Do not add telemetry or remote analytics by default.
- Keep the architecture capable of supporting data export and import in the
  future, but do not treat them as current product requirements.
- Features should remain usable without proprietary services whenever
  technically practical.

## Project structure

- `app/src/main/java/com/universalwill/sportoss/ui/screens/<feature>/` contains
  feature packages.
- Keep a feature's route, screen, UI contract, and ViewModel close together in
  the same package.
- `ui/navigation/` contains top-level navigation and the shared application
  scaffold.
- `ui/theme/` contains the Compose theme and design tokens.
- JVM unit tests belong in `app/src/test/`.
- Instrumented and Compose UI tests belong in `app/src/androidTest/`.

## Architecture

Use unidirectional data flow (UDF) and state hoisting.

A stateful feature should normally contain:

- `FeatureRoute` to obtain dependencies and collect lifecycle-aware state.
- `FeatureScreen` to render immutable state and emit actions.
- `FeatureUiState` for the complete observable screen state.
- `FeatureAction` (sealed interface) for user and UI events.
- `FeatureViewModel` for business state and behavior.

Standard screen signature pattern:

```kotlin
@Composable
fun FeatureScreen(
    state: FeatureUiState,
    onAction: (FeatureAction) -> Unit,
    modifier: Modifier = Modifier,
)
```

Rules:

- State flows down; events flow up via a single action callback.
- Expose ViewModel state as a read-only `StateFlow`.
- Prefer immutable state models and pure state transformations.
- Do not pass ViewModels into reusable UI components or `FeatureScreen`.
- Do not keep persistent business state in composables.
- Keep short-lived visual state in the lowest composable that needs it.
- Keep platform and third-party UI controllers in the UI layer unless another
  layer genuinely needs to control them.

## Dependency injection

- Use Hilt and constructor injection.
- Hilt ViewModels use `@HiltViewModel` and an `@Inject` constructor.
- Obtain Hilt ViewModels in route composables with `hiltViewModel()`.
- Scope Navigation 3 ViewModels to their `NavEntry` with the ViewModel store
  entry decorator.
- Create Hilt modules only for interfaces, third-party types, or objects that
  cannot use constructor injection.

## Navigation

- Use Jetpack Navigation 3 and serializable `NavKey` destinations.
- Keep the common `Scaffold` in the application navigation layer.
- Use Material 3 navigation components for top-level destinations.
- Preserve a separate back stack and state for each top-level destination.
- Keep navigation decisions outside reusable screen content.

## Compose UI

- Prefer Material 3 components and `MaterialTheme` tokens.
- Reusable composables accept a `Modifier`.
- Required parameters come first; `modifier: Modifier = Modifier` is the first
  optional parameter.
- Add previews for reusable UI and important screen states.
- Previews must not initialize sensors, location providers, network clients,
  databases, or other runtime-only services.
- Avoid hardcoded colors when a suitable theme token exists.
- Move user-facing text to string resources when it is no longer temporary
  prototype copy.

## MapLibre

- Keep MapLibre camera, rendering, and gesture state in the UI layer.
- Keep sports activity and recording state independent of MapLibre.
- Never initialize sensor-backed heading or location providers in previews.
- Modify map layers only after the style reaches `StyleLoadState.Ready`.

## Dependencies and Gradle

- Declare versions and aliases in `gradle/libs.versions.toml`.
- Do not hardcode dependency versions in module build files.
- Use the Gradle wrapper rather than a globally installed Gradle.
- The project targets Java 17.
- Check AGP, Kotlin, Compose, Hilt, and KSP compatibility before upgrades.
- Do not mix unrelated dependency upgrades into another change.

## Build and verification

Use the Gradle wrapper for your environment:
- Windows (PowerShell): `.\gradlew.bat <task>`
- Linux / macOS / WSL: `./gradlew <task>`

Standard verification tasks:

```shell
:app:compileDebugKotlin
:app:testDebugUnitTest
:app:assembleDebug
:app:lintDebug
```
*(If code formatting tools like Spotless or Ktlint are configured, run their format check before reporting completion).*

Run checks proportional to the change:

- Kotlin or architecture changes: compile and unit tests.
- UI changes: compile, relevant previews or UI tests, and assemble.
- Gradle, Hilt, KSP, or manifest changes: assemble and lint.
- Significant changes intended for commit: unit tests, assemble, and lint.

Do not report a command as successful unless it was actually executed.

## Testing

- Prefer fast deterministic unit tests for reducers, formatters, state
  transitions, use cases, and ViewModels.
- Use `kotlinx-coroutines-test` and virtual time for coroutine tests.
- Do not use real delays in unit tests.
- Test observable behavior instead of private implementation details.
- Add a regression test when fixing reproducible business-logic bugs.
- Avoid testing layout coordinates, third-party internals, or live map tiles.
- Add screenshot tests only after the relevant UI has stabilized.

## Secrets and local configuration

- Keep API keys and machine-specific values in `local.properties` or another
  ignored local file.
- Never commit credentials, tokens, signing files, or generated secrets.
- Inspect new and modified configuration files for secrets before staging.
- Commit IDE files only when they are intentionally shared project settings.

## Working tree safety

- Existing uncommitted changes belong to the user.
- Preserve unrelated user changes and work around overlapping edits.
- Do not stage or commit unrelated changes.
- Do not use destructive Git commands such as `git reset --hard`.
- Use `apply_patch` for deliberate source-file edits.

## Git commits

- Commit only when explicitly requested.
- Keep one logical change per commit.
- Use Conventional Commits:

  `<type>(<scope>): <description>`

- Use an imperative English description without a trailing period.
- Keep the subject under 72 characters.
- Match the repository's established commit style.
- Never amend, force-push, push, or skip hooks unless explicitly requested.
- After committing, report the hash, subject, verification performed, and any
  remaining uncommitted changes.

## Definition of done

A change is complete when:

- The requested behavior is implemented.
- The architecture and dependency rules above are preserved.
- Relevant tests are added or updated.
- Appropriate build and quality checks pass without suppressed warnings.
- No secrets or unrelated files are included.
- Remaining limitations or unverified behavior are reported clearly.
