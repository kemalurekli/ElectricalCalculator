package com.kemalurekli.electricalcalculator.features.forum

import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthFailure
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumAccountViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.isPlausibleEmail
import com.kemalurekli.electricalcalculator.testing.FakeForumAuthRepository
import com.kemalurekli.electricalcalculator.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

/**
 * Signing in with an address and a six-digit code.
 *
 * The rate limits are the reason most of this exists. Supabase allows one code
 * per address per minute and thirty an hour across the whole project, and
 * answers 429 after that — so a request spent on an obvious typo is a request
 * a real reader does not get.
 */
class ForumEmailSignInTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private fun viewModel(auth: FakeForumAuthRepository) = ForumAccountViewModel(auth)

    @Test
    fun `an address that cannot work never reaches the server`() = runTest {
        val auth = FakeForumAuthRepository()
        val viewModel = viewModel(auth)

        viewModel.onEmailChange("kemal.example.com")
        viewModel.onSendCode()
        advanceUntilIdle()

        assertTrue(auth.codesRequested.isEmpty(), "a missing @ must not spend a rate-limited request")
        assertEquals(ForumAuthFailure.INVALID_EMAIL, viewModel.failure.value)
    }

    @Test
    fun `sending a code moves on to asking for it`() = runTest {
        val auth = FakeForumAuthRepository()
        val viewModel = viewModel(auth)

        viewModel.onEmailChange("kemal@example.com")
        viewModel.onSendCode()
        advanceUntilIdle()

        assertEquals(listOf("kemal@example.com"), auth.codesRequested)
        val step = assertIs<ForumAccountViewModel.EmailStep.Code>(viewModel.emailStep.value)
        assertEquals("kemal@example.com", step.email)
    }

    @Test
    fun `the code field takes six digits and nothing else`() = runTest {
        val viewModel = viewModel(FakeForumAuthRepository())

        viewModel.onCodeChange("12a3 45b6789")

        assertEquals("123456", viewModel.code.value)
    }

    @Test
    fun `a half-typed code is not sent`() = runTest {
        val auth = FakeForumAuthRepository()
        val viewModel = viewModel(auth)
        viewModel.onEmailChange("kemal@example.com")
        viewModel.onSendCode()
        advanceUntilIdle()

        viewModel.onCodeChange("123")
        viewModel.onVerifyCode()
        advanceUntilIdle()

        // Still waiting for the code rather than signed in, and no request was
        // spent proving that three digits are not six.
        assertIs<ForumAccountViewModel.EmailStep.Code>(viewModel.emailStep.value)
    }

    @Test
    fun `a good code signs the reader in`() = runTest {
        val auth = FakeForumAuthRepository()
        val viewModel = viewModel(auth)
        viewModel.onEmailChange("kemal@example.com")
        viewModel.onSendCode()
        advanceUntilIdle()

        viewModel.onCodeChange("123456")
        viewModel.onVerifyCode()
        advanceUntilIdle()

        assertEquals("kemal@example.com", (auth.codesRequested.single()))
        assertIs<ForumAccountViewModel.EmailStep.Address>(viewModel.emailStep.value)
    }

    @Test
    fun `an unrelated failure is not reported as a bad code`() = runTest {
        // The word "invalid" is in this message, and the first version of the
        // mapping searched for exactly that — so a rejected key, a
        // misconfigured project and a 500 all reached the reader as "the code
        // is wrong, or it has expired". Confident, wrong, and it sent them to
        // check the one thing that was fine.
        val auth = FakeForumAuthRepository()
        auth.emailFailure = IllegalStateException("Invalid API key")
        val viewModel = viewModel(auth)

        viewModel.onEmailChange("kemal@example.com")
        viewModel.onSendCode()
        advanceUntilIdle()

        assertEquals(ForumAuthFailure.UNKNOWN, viewModel.failure.value)
    }

    @Test
    fun `tapping send again does not spend the codes everyone shares`() = runTest {
        val auth = FakeForumAuthRepository()
        val viewModel = viewModel(auth)
        viewModel.onEmailChange("kemal@example.com")
        viewModel.onSendCode()
        // `runCurrent` rather than `advanceUntilIdle`: the latter runs the
        // clock to the end of the cooldown, which is the very thing under test.
        runCurrent()

        // Impatience, five times over. The per-hour limit is project-wide, so
        // these would not be this reader's requests to spend.
        repeat(5) { viewModel.onSendCode() }
        runCurrent()

        assertEquals(1, auth.codesRequested.size, "only the first send should have left the app")
    }

    @Test
    fun `the wait is counted down and then lifted`() = runTest {
        val auth = FakeForumAuthRepository()
        val viewModel = viewModel(auth)
        viewModel.onEmailChange("kemal@example.com")
        viewModel.onSendCode()
        runCurrent()

        assertEquals(60, viewModel.resendIn.value, "the wait matches Supabase's own per-address limit")

        advanceTimeBy(59_000)
        runCurrent()
        assertTrue(viewModel.resendIn.value > 0, "still waiting a second before the minute is up")

        advanceUntilIdle()
        assertEquals(0, viewModel.resendIn.value)

        viewModel.onSendCode()
        runCurrent()
        assertEquals(2, auth.codesRequested.size, "and then another code can be asked for")
    }

    @Test
    fun `the address check rejects only what could not possibly work`() {
        listOf(
            "kemal@example.com",
            "kemal+forum@example.co.uk",
            "k@a.io",
            "o'brien@example.com",
        ).forEach { assertTrue(it.isPlausibleEmail(), "$it is a real address shape") }

        listOf(
            "",
            "kemal",
            "kemal@",
            "@example.com",
            "kemal@example",
            "kemal @example.com",
            "a@b@example.com",
        ).forEach { assertTrue(!it.isPlausibleEmail(), "$it cannot be delivered to") }
    }
}
