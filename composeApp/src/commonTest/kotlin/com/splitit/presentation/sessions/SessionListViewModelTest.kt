@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.sessions

import com.splitit.domain.usecase.DeleteSessionUseCase
import com.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.splitit.domain.usecase.ObserveSessionsUseCase
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemorySessionRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.session
import com.splitit.testutils.testLocalizationService
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionListViewModelTest {
    @Test
    fun loadsEmptyStateAfterInitialRefresh() = runViewModelTest {
        val repository = InMemorySessionRepository()
        val viewModel = viewModel(repository)

        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.sessions.isEmpty())
        assertEquals(null, viewModel.state.value.errorMessage)
    }

    @Test
    fun exposesLoadErrors() = runViewModelTest {
        val repository = InMemorySessionRepository()
        repository.getError = IllegalStateException("database unavailable")
        val viewModel = viewModel(repository)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("database unavailable", viewModel.state.value.errorMessage)
    }

    @Test
    fun deletesSessionAndRefreshesList() = runViewModelTest {
        val repository = InMemorySessionRepository(listOf(session()))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.delete(TestIds.session)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.sessions.isEmpty())
        assertEquals(1, repository.deleteCalls)
    }

    @Test
    fun filtersSessionsByTitleAndDescription() = runViewModelTest {
        val repository = InMemorySessionRepository(
            listOf(
                session(title = "Beach trip"),
                session(
                    id = TestIds.secondSession,
                    title = "Monthly bills",
                    description = "Shared home expenses",
                ),
            ),
        )
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("BEACH")
        assertEquals(listOf(TestIds.session), viewModel.state.value.visibleSessions.map { it.id })

        viewModel.onSearchQueryChange("home")
        assertEquals(listOf(TestIds.secondSession), viewModel.state.value.visibleSessions.map { it.id })

        viewModel.onSearchQueryChange("missing")
        assertTrue(viewModel.state.value.visibleSessions.isEmpty())
    }

    @Test
    fun marksGroupPendingWhenItHasUnsettledExpenses() = runViewModelTest {
        val sessionRepository = InMemorySessionRepository(
            listOf(
                session(
                    participantIds = setOf(TestIds.alice),
                    expenseIds = setOf(TestIds.expense),
                ),
            ),
        )
        val participantRepository = InMemoryParticipantRepository(listOf(participant()))
        val expenseRepository = InMemoryExpenseRepository(listOf(expense()))
        val viewModel = SessionListViewModel(
            observeSessions = ObserveSessionsUseCase(sessionRepository),
            observeSessionDetails = ObserveSessionDetailsUseCase(
                sessionRepository,
                participantRepository,
                expenseRepository,
                InMemorySettlementRepository(),
            ),
            deleteSession = DeleteSessionUseCase(sessionRepository),
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertEquals(setOf(TestIds.session), viewModel.state.value.pendingSessionIds)
        assertEquals(
            listOf(TestIds.alice),
            viewModel.state.value.participantsBySession[TestIds.session]?.map { it.id },
        )
    }

    private fun viewModel(repository: InMemorySessionRepository): SessionListViewModel {
        return SessionListViewModel(
            observeSessions = ObserveSessionsUseCase(repository),
            observeSessionDetails = ObserveSessionDetailsUseCase(
                repository,
                InMemoryParticipantRepository(),
                InMemoryExpenseRepository(),
                InMemorySettlementRepository(),
            ),
            deleteSession = DeleteSessionUseCase(repository),
            localization = testLocalizationService,
        )
    }
}
