package com.kemalurekli.electricalcalculator.core.feedback

import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackFailure
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackReport
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackRepository
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackResult
import com.kemalurekli.electricalcalculator.core.feedback.presentation.ReportIssueViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * What happens to three sentences somebody has just typed.
 *
 * That is the whole of what this view model is for: the send is one call, and
 * everything worth testing is about not losing the text around it.
 */
class ReportIssueViewModelTest {

    private class FakeRepository(
        var result: FeedbackResult = FeedbackResult.Sent,
    ) : FeedbackRepository {
        val sent = mutableListOf<FeedbackReport>()
        override suspend fun send(report: FeedbackReport): FeedbackResult {
            sent += report
            return result
        }
    }

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `a failed send keeps what was written`() = runTest {
        // The reason the text lives here rather than in the composable. A
        // report that is thrown away because the train went into a tunnel is
        // a report nobody writes twice.
        val repository = FakeRepository(FeedbackResult.Failed(FeedbackFailure.NETWORK))
        val model = ReportIssueViewModel(repository)
        model.onMessageChange("Formulde Q isareti ters")

        model.onSend("calculator:power", "tr")
        advanceUntilIdle()

        assertEquals("Formulde Q isareti ters", model.state.value.message)
        assertEquals(FeedbackFailure.NETWORK, model.state.value.failure)
        assertFalse(model.state.value.isSending)
    }

    @Test
    fun `a sent report leaves nothing behind`() = runTest {
        val repository = FakeRepository()
        val model = ReportIssueViewModel(repository)
        model.onMessageChange("Tablodaki kesit yanlis")

        model.onSend("reference:cable_ampacity", "tr")
        advanceUntilIdle()

        assertTrue(model.state.value.isSent)
        assertEquals("", model.state.value.message)
    }

    @Test
    fun `typing clears the last failure`() = runTest {
        // The error is about the send that failed, not about the text. Leaving
        // it on screen while somebody edits reads as a rejection of what they
        // are writing.
        val repository = FakeRepository(FeedbackResult.Failed(FeedbackFailure.TOO_MANY))
        val model = ReportIssueViewModel(repository)
        model.onMessageChange("Birinci deneme")
        model.onSend("converter", "en")
        advanceUntilIdle()

        model.onMessageChange("Birinci deneme, biraz daha")

        assertNull(model.state.value.failure)
    }

    @Test
    fun `too short to mean anything is not sent`() = runTest {
        val repository = FakeRepository()
        val model = ReportIssueViewModel(repository)
        model.onMessageChange("yok")

        assertFalse(model.state.value.canSend)
        model.onSend("theory:ohms_law", "tr")
        advanceUntilIdle()

        assertTrue(repository.sent.isEmpty(), "the round trip would only be refused by the table")
    }

    @Test
    fun `pressing send twice sends one report`() = runTest {
        val repository = FakeRepository()
        val model = ReportIssueViewModel(repository)
        model.onMessageChange("Ayni mesaji iki kez gondermesin")

        model.onSend("calculator:power", "tr")
        model.onSend("calculator:power", "tr")
        advanceUntilIdle()

        assertEquals(1, repository.sent.size)
    }

    @Test
    fun `the report carries the screen and the language it was read in`() = runTest {
        val repository = FakeRepository()
        val model = ReportIssueViewModel(repository)
        model.onMessageChange("  Bosluklar kirpilsin  ")

        model.onSend("theory:kirchhoff", "pl")
        advanceUntilIdle()

        val report = repository.sent.single()
        assertEquals("theory:kirchhoff", report.area)
        assertEquals("pl", report.locale)
        assertEquals("Bosluklar kirpilsin", report.message)
    }
}
