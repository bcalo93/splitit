@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.participants

import com.splitit.domain.usecase.AddParticipantUseCase
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.usecase.RemoveParticipantUseCase
import com.splitit.domain.usecase.UpdateParticipantUseCase
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemoryGroupRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestClock
import com.splitit.testutils.TestIdGenerator
import com.splitit.testutils.TestIds
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.group
import com.splitit.testutils.testLocalizationService
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParticipantsViewModelTest {
    @Test
    fun loadsParticipantsAndSupportsEditingCancellation() = runViewModelTest {
        val alice = participant(TestIds.alice, name = "Alice", avatarColor = "#111111")
        val repository = InMemoryParticipantRepository(listOf(alice))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(listOf(alice), viewModel.state.value.participants)
        viewModel.startEditing(alice)
        assertEquals(alice.name, viewModel.state.value.name)
        assertEquals(alice.avatarColor, viewModel.state.value.selectedColor)
        viewModel.cancelEditing()

        assertEquals("", viewModel.state.value.name)
        assertEquals(null, viewModel.state.value.editingParticipantId)
    }

    @Test
    fun validatesAndAddsParticipant() = runViewModelTest {
        val repository = InMemoryParticipantRepository()
        val viewModel = createViewModel(repository, participantId = TestIds.charlie)
        advanceUntilIdle()

        viewModel.save()
        assertEquals("Enter a participant name.", viewModel.state.value.nameError)

        viewModel.onNameChange("  Charlie  ")
        viewModel.onColorSelected("#ABCDEF")
        viewModel.save()
        advanceUntilIdle()

        assertTrue(repository.savedParticipants.any { it.name == "Charlie" })
        assertEquals("", viewModel.state.value.name)
        assertEquals(null, viewModel.state.value.editingParticipantId)
        assertTrue(viewModel.state.value.saveSucceeded)
        viewModel.consumeSaveSuccess()
        assertFalse(viewModel.state.value.saveSucceeded)
    }

    @Test
    fun showsSpecificErrorWhenDeletingUsedParticipant() = runViewModelTest {
        val repository = InMemoryParticipantRepository(listOf(participant()))
        repository.usedParticipantIds += TestIds.alice
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.delete(TestIds.alice)
        advanceUntilIdle()

        assertEquals(
            "Participant cannot be removed because it is used by expenses.",
            viewModel.state.value.errorMessage,
        )
        assertEquals(0, repository.deleteCalls)
    }

    private fun createViewModel(
        participantRepository: InMemoryParticipantRepository,
        participantId: com.splitit.domain.value.ParticipantId = TestIds.alice,
    ): ParticipantsViewModel {
        val groupRepository = InMemoryGroupRepository(listOf(group()))
        return ParticipantsViewModel(
            groupId = TestIds.group,
            observeGroupDetails = ObserveGroupDetailsUseCase(
                groupRepository,
                participantRepository,
                InMemoryExpenseRepository(),
                InMemorySettlementRepository(),
            ),
            addParticipant = AddParticipantUseCase(
                groupRepository,
                participantRepository,
                TestIdGenerator(participantId = participantId),
                TestClock(20L),
            ),
            updateParticipant = UpdateParticipantUseCase(participantRepository, TestClock(20L)),
            removeParticipant = RemoveParticipantUseCase(participantRepository),
            localization = testLocalizationService,
        )
    }
}
