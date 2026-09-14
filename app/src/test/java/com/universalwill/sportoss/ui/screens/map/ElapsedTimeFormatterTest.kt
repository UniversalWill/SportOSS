package com.universalwill.sportoss.ui.screens.map

import org.junit.Assert.assertEquals
import org.junit.Test

class ElapsedTimeFormatterTest {
    @Test
    fun `formats elapsed time boundaries`() {
        val cases = mapOf(
            0L to "00:00:00",
            59L to "00:00:59",
            60L to "00:01:00",
            3_599L to "00:59:59",
            3_600L to "01:00:00",
            90_061L to "25:01:01",
        )

        cases.forEach { (seconds, expected) ->
            assertEquals(expected, formatElapsedTime(seconds))
        }
    }
}
