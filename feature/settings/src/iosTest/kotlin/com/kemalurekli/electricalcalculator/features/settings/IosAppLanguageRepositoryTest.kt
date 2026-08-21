package com.kemalurekli.electricalcalculator.features.settings

import com.kemalurekli.electricalcalculator.core.domain.model.AppLanguage
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The language choice is stored under `AppleLanguages`, which is iOS's own key
 * rather than one this app made up — so these tests are as much about the
 * platform holding up its end as about the class.
 *
 * Storage is exercised through a named suite instead of the app's own domain,
 * because a Kotlin/Native test binary has no bundle identifier and therefore no
 * app domain to write into. The class is the real one and the domain lookup is
 * the real one; only the name differs from what an installed app would use.
 */
class IosAppLanguageRepositoryTest {

    private val suite = NSUserDefaults(suiteName = SUITE)

    @AfterTest
    fun leaveNothingBehind() {
        suite.removePersistentDomainForName(SUITE)
        NSUserDefaults.standardUserDefaults.removeObjectForKey(APPLE_LANGUAGES)
    }

    private fun repository() = IosAppLanguageRepository(suite, SUITE)

    @Test
    fun `the choice survives a new repository — which is what a relaunch is`() {
        repository().setLanguage(AppLanguage.TURKISH)

        assertEquals(AppLanguage.TURKISH, repository().language.value)
    }

    @Test
    fun `following the phone is stored as no choice at all`() {
        repository().setLanguage(AppLanguage.TURKISH)
        repository().setLanguage(AppLanguage.SYSTEM)

        // Not "English", and not an empty list: the key has to be gone, or the
        // app would pin itself to whatever the phone said the day it was set.
        assertNull(suite.persistentDomainForName(SUITE)?.get(APPLE_LANGUAGES))
        assertEquals(AppLanguage.SYSTEM, repository().language.value)
    }

    @Test
    fun `the flow reports the new language to whoever is listening`() {
        val repository = repository()

        repository.setLanguage(AppLanguage.ENGLISH)

        assertEquals(AppLanguage.ENGLISH, repository.language.value)
    }

    @Test
    fun `a language the app does not ship is not honoured`() {
        // Written the way iOS Settings would if the app ever shipped German and
        // then withdrew it. The reader should land back on the phone's
        // language, not on a screen of missing strings.
        suite.setObject(listOf("de"), forKey = APPLE_LANGUAGES)

        assertEquals(AppLanguage.SYSTEM, repository().language.value)
    }

    @Test
    fun `writing the key moves the language iOS reports`() {
        // The one thing the suite cannot show, done against the standard
        // defaults where the app writes for real. Compose Resources picks its
        // `values-*` folder from this list; if the write did not reach it, the
        // app would keep its old strings and the whole feature would be a
        // radio button that does nothing.
        NSUserDefaults.standardUserDefaults.setObject(listOf("tr"), forKey = APPLE_LANGUAGES)

        assertEquals("tr", NSLocale.preferredLanguages.firstOrNull() as? String)
    }

    private companion object {
        const val SUITE = "com.kemalurekli.electricalcalculator.languagetest"
        const val APPLE_LANGUAGES = "AppleLanguages"
    }
}
