@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.participants

import com.splitit.domain.model.Participant
import com.splitit.domain.usecase.AddParticipantParams
import com.splitit.domain.usecase.GroupDetails
import com.splitit.domain.usecase.RemoveParticipantParams
import com.splitit.domain.usecase.UpdateParticipantParams
import com.splitit.testutils.TestIds
import com.splitit.testutils.group
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.testLocalizationService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParticipantsViewModelTest {
    @Test
    fun loadsParticipantsAndSupportsEditingCancellation() = runViewModelTest {
        val alice = participant(TestIds.alice, name = "Alice", avatarColor = "#111111")
        val viewModel = createViewModel(participants = listOf(alice))
        advanceUntilIdle()

        assertEquals(listOf(alice), viewModel.state.value.participants)
        viewModel.startEditing(alice)
        assertEquals(alice.name, viewModel.state.value.name)
        assertEquals(alice.avatarColor, viewModel.state.value.selectedColor)
        viewModel.cancelEditing()

        assertEquals("", viewModel.state.value.name)
        assertNull(viewModel.state.value.editingParticipantId)
    }

    @Test
    fun validatesAndAddsParticipant() = runViewModelTest {
        val addParticipant = mockk<com.splitit.domain.usecase.AddParticipantUseCase>()
        coEvery { addParticipant.invoke(any()) } returns participant(TestIds.charlie, name = "Charlie", avatarColor = "#ABCDEF")

        val viewModel = createViewModel(addParticipant = addParticipant)
        advanceUntilIdle()

        viewModel.save()
        assertEquals("Enter a participant name.", viewModel.state.value.nameError)

        viewModel.onNameChange("  Charlie  ")
        viewModel.onColorSelected("#ABCDEF")
        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 1) { addParticipant.invoke(any()) }
        assertEquals("", viewModel.state.value.name)
        assertNull(viewModel.state.value.editingParticipantId)
        assertTrue(viewModel.state.value.saveSucceeded)
        viewModel.consumeSaveSuccess()
        assertFalse(viewModel.state.value.saveSucceeded)
    }

    @Test
    fun showsSpecificErrorWhenDeletingUsedParticipant() = runViewModelTest {
        val removeParticipant = mockk<com.splitit.domain.usecase.RemoveParticipantUseCase>()
        coEvery { removeParticipant.invoke(any()) } throws IllegalArgumentException(
            "Participant ${TestIds.alice.value} cannot be removed because it is used by expenses.",
        )

        val viewModel = createViewModel(removeParticipant = removeParticipant)
        advanceUntilIdle()

        viewModel.delete(TestIds.alice)
        advanceUntilIdle()

        assertEquals(
            "Participant cannot be removed because it is used by expenses.",
            viewModel.state.value.errorMessage,
        )
        coVerify(exactly = 1) { removeParticipant.invoke(RemoveParticipantParams(TestIds.alice)) }
    }

    @Test
    fun updatesParticipantWhenEditing() = runViewModelTest {
        val alice = participant(TestIds.alice, name = "Alice")
        val updateParticipant = mockk<com.splitit.domain.usecase.UpdateParticipantUseCase>()
        coEvery { updateParticipant.invoke(any()) } returns alice.copy(name = "Alice Updated")

        val viewModel = createViewModel(
            participants = listOf(alice),
            updateParticipant = updateParticipant,
        )
        advanceUntilIdle()

        viewModel.startEditing(alice)
        viewModel.onNameChange("Alice Updated")
        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 1) { updateParticipant.invoke(any()) }
    }

    private fun createViewModel(
        participants: List<Participant> = listOf(participant()),
        addParticipant: com.splitit.domain.usecase.AddParticipantUseCase = mockk<com.splitit.domain.usecase.AddParticipantUseCase>(),
        updateParticipant: com.splitit.domain.usecase.UpdateParticipantUseCase = mockk<com.splitit.domain.usecase.UpdateParticipantUseCase>(),
        removeParticipant: com.splitit.domain.usecase.RemoveParticipantUseCase = mockk<com.splitit.domain.usecase.RemoveParticipantUseCase>(),
    ): ParticipantsViewModel {
        val observeGroupDetails = mockk<com.splitit.domain.usecase.ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails.invoke(any()) } returns GroupDetails(
            group = group(),
            participants = participants,
            expenses = emptyList(),
            latestSettlement = null,
        )
        return ParticipantsViewModel(
            groupId = TestIds.group,
            observeGroupDetails = observeGroupDetails,
            addParticipant = addParticipant,
            updateParticipant = updateParticipant,
            removeParticipant = removeParticipant,
            localization = testLocalizationService,
        )
    }
}
