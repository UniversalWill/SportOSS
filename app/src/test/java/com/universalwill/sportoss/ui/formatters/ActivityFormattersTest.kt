package com.universalwill.sportoss.ui.formatters

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityFormattersTest {
    @Test
    fun `formats activity date in requested locale and zone`() {
        val epochMillis = LocalDateTime.of(2026, 9, 14, 10, 30)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

        val result = formatActivityDate(
            epochMillis = epochMillis,
            zoneId = ZoneOffset.UTC,
            locale = Locale.forLanguageTag("ru"),
        )

        assertEquals("14 сентября 2026", result)
    }
}
