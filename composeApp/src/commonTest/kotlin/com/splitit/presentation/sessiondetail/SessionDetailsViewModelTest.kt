@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.sessiondetail

import com.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemorySessionRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestIds
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.session
import com.splitit.testutils.testLocalizationService
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SessionDetailsViewModelTest {
    @Test
    fun loadsDetailsAndCanRefresh() = runViewModelTest {
        val sessionRepository = InMemorySessionRepository(listOf(session()))
        val participantRepository = InMemoryParticipantRepository(listOf(participant()))
        val viewModel = SessionDetailsViewModel(
            sessionId = TestIds.session,
            observeSessionDetails = ObserveSessionDetailsUseCase(
                sessionRepository,
                participantRepository,
                InMemoryExpenseRepository(),
                InMemorySettlementRepository(),
            ),
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(TestIds.session, viewModel.state.value.details?.session?.id)
        assertEquals(1, viewModel.state.value.details?.participants?.size)

        viewModel.refresh()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun exposesMissingSessionError() = runViewModelTest {
        val viewModel = SessionDetailsViewModel(
            sessionId = TestIds.session,
            observeSessionDetails = ObserveSessionDetailsUseCase(
                InMemorySessionRepository(),
                InMemoryParticipantRepository(),
                InMemoryExpenseRepository(),
                InMemorySettlementRepository(),
            ),
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Session session was not found.", viewModel.state.value.errorMessage)
    }
}
