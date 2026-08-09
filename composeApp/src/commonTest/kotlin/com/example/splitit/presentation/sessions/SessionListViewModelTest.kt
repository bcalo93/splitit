@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.splitit.presentation.sessions

import com.example.splitit.domain.usecase.DeleteSessionUseCase
import com.example.splitit.domain.usecase.ObserveSessionsUseCase
import com.example.splitit.testutils.InMemorySessionRepository
import com.example.splitit.testutils.TestIds
import com.example.splitit.testutils.runViewModelTest
import com.example.splitit.testutils.session
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

    private fun viewModel(repository: InMemorySessionRepository): SessionListViewModel {
        return SessionListViewModel(
            observeSessions = ObserveSessionsUseCase(repository),
            deleteSession = DeleteSessionUseCase(repository),
        )
    }
}
