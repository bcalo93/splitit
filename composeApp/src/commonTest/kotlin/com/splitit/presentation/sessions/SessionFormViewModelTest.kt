@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.sessions

import com.splitit.domain.usecase.CreateSessionUseCase
import com.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.splitit.domain.usecase.UpdateSessionUseCase
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemorySessionRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestClock
import com.splitit.testutils.TestIdGenerator
import com.splitit.testutils.TestIds
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.session
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
        sessionId: com.splitit.domain.value.SessionId? = null,
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
