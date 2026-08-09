@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.splitit.presentation.sessiondetail

import com.example.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.example.splitit.testutils.InMemoryExpenseRepository
import com.example.splitit.testutils.InMemoryParticipantRepository
import com.example.splitit.testutils.InMemorySessionRepository
import com.example.splitit.testutils.InMemorySettlementRepository
import com.example.splitit.testutils.TestIds
import com.example.splitit.testutils.participant
import com.example.splitit.testutils.runViewModelTest
import com.example.splitit.testutils.session
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
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Session session was not found.", viewModel.state.value.errorMessage)
    }
}
