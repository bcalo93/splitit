@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.splitit.presentation.sessions

import com.example.splitit.domain.usecase.CreateSessionUseCase
import com.example.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.example.splitit.domain.usecase.UpdateSessionUseCase
import com.example.splitit.testutils.InMemoryExpenseRepository
import com.example.splitit.testutils.InMemoryParticipantRepository
import com.example.splitit.testutils.InMemorySessionRepository
import com.example.splitit.testutils.InMemorySettlementRepository
import com.example.splitit.testutils.TestClock
import com.example.splitit.testutils.TestIdGenerator
import com.example.splitit.testutils.TestIds
import com.example.splitit.testutils.runViewModelTest
import com.example.splitit.testutils.session
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionFormViewModelTest {
    @Test
    fun validatesTitleBeforeLaunchingSave() = runViewModelTest {
        val repository = InMemorySessionRepository()
        val viewModel = createViewModel(repository)

        viewModel.save()

        assertEquals("Enter a session name.", viewModel.state.value.titleError)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun createsSessionAndExposesSavedId() = runViewModelTest {
        val repository = InMemorySessionRepository()
        val viewModel = createViewModel(repository)

        viewModel.onTitleChange("  Weekend  ")
        viewModel.onDescriptionChange("  Notes  ")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(TestIds.session, viewModel.state.value.savedSessionId)
        assertFalse(viewModel.state.value.isSaving)
        assertEquals("Weekend", repository.savedSessions.single().title)
        assertEquals("Notes", repository.savedSessions.single().description)

        viewModel.consumeSavedSession()
        assertEquals(null, viewModel.state.value.savedSessionId)
    }

    @Test
    fun loadsAndUpdatesExistingSession() = runViewModelTest {
        val original = session(title = "Old title", description = "Old description")
        val repository = InMemorySessionRepository(listOf(original))
        val viewModel = createViewModel(repository, original.id)
        advanceUntilIdle()

        assertEquals("Old title", viewModel.state.value.title)
        assertEquals("Old description", viewModel.state.value.description)

        viewModel.onTitleChange("New title")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(original.id, viewModel.state.value.savedSessionId)
        assertEquals("New title", repository.savedSessions.single().title)
    }

    private fun createViewModel(
        sessionRepository: InMemorySessionRepository,
        sessionId: com.example.splitit.domain.value.SessionId? = null,
    ): SessionFormViewModel {
        return SessionFormViewModel(
            sessionId = sessionId,
            createSession = CreateSessionUseCase(
                sessionRepository,
                TestIdGenerator(),
                TestClock(20L),
            ),
            updateSession = UpdateSessionUseCase(sessionRepository, TestClock(20L)),
            observeSessionDetails = ObserveSessionDetailsUseCase(
                sessionRepository = sessionRepository,
                participantRepository = InMemoryParticipantRepository(),
                expenseRepository = InMemoryExpenseRepository(),
                settlementRepository = InMemorySettlementRepository(),
            ),
        )
    }
}
