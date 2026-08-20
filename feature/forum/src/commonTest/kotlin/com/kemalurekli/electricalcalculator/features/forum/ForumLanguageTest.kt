package com.kemalurekli.electricalcalculator.features.forum

import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumLanguage
import kotlin.test.assertEquals
import kotlin.test.Test

/**
 * Which forum a reader is shown.
 *
 * The split is strict and there is no way to see the other side, so getting
 * this wrong does not degrade the experience — it hides the entire feature
 * behind a language the reader did not choose.
 */
class ForumLanguageTest {

    @Test
    fun `an explicit app language decides — whatever the device says`() {
        // Someone reading the app in Turkish on an English phone is here for
        // the Turkish forum.
        assertEquals(
            ForumLanguage.TURKISH,
            ForumLanguage.forApp(AppLanguage.TURKISH, "en"),
        )
        assertEquals(
            ForumLanguage.ENGLISH,
            ForumLanguage.forApp(AppLanguage.ENGLISH, "tr"),
        )
    }

    @Test
    fun `following the system means following the device`() {
        assertEquals(
            ForumLanguage.TURKISH,
            ForumLanguage.forApp(AppLanguage.SYSTEM, "tr"),
        )
        assertEquals(
            ForumLanguage.ENGLISH,
            ForumLanguage.forApp(AppLanguage.SYSTEM, "en"),
        )
    }

    @Test
    fun `every other language falls to English — as the rest of the app does`() {
        // The app ships two translations. A German or Arabic device already
        // reads English everywhere else; the forum must not be the one place
        // that shows them nothing.
        listOf("de-DE", "ar-EG", "ja-JP", "fr-FR").forEach { tag ->
            assertEquals(
                ForumLanguage.ENGLISH,
                ForumLanguage.forApp(AppLanguage.SYSTEM, tag.substringBefore('-')),
                tag,
            )
        }
    }

    @Test
    fun `the codes are the ones the database checks against`() {
        // forum_categories.language carries a CHECK constraint on exactly
        // these two strings; a mismatch here is a row the app can never read.
        assertEquals("tr", ForumLanguage.TURKISH.code)
        assertEquals("en", ForumLanguage.ENGLISH.code)
    }
}
