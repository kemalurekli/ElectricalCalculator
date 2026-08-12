package com.kemalurekli.electricalcalculator.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `a null or blank tag means follow the system`() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTagOrSystem(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTagOrSystem(""))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTagOrSystem("   "))
    }

    @Test
    fun `every supported tag round trips`() {
        AppLanguage.entries
            .filter { it.languageTag != null }
            .forEach { language ->
                assertEquals(language, AppLanguage.fromTagOrSystem(language.languageTag))
            }
    }

    @Test
    fun `a region qualified tag resolves to its language`() {
        // Android may hand back "tr-TR" even though only "tr" is declared.
        assertEquals(AppLanguage.TURKISH, AppLanguage.fromTagOrSystem("tr-TR"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTagOrSystem("en-GB"))
    }

    @Test
    fun `tag matching is case insensitive`() {
        assertEquals(AppLanguage.TURKISH, AppLanguage.fromTagOrSystem("TR"))
    }

    @Test
    fun `an unsupported tag falls back to system rather than throwing`() {
        // Reachable if a shipped language is withdrawn while a device still
        // holds the old preference.
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTagOrSystem("xx"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTagOrSystem("de-DE"))
    }

    @Test
    fun `system is the only entry without a tag`() {
        val untagged = AppLanguage.entries.filter { it.languageTag == null }

        assertEquals(listOf(AppLanguage.SYSTEM), untagged)
    }

    @Test
    fun `language tags are unique and lowercase`() {
        val tags = AppLanguage.entries.mapNotNull { it.languageTag }

        assertEquals(tags.size, tags.distinct().size)
        assertTrue(tags.all { it == it.lowercase() })
    }
}
