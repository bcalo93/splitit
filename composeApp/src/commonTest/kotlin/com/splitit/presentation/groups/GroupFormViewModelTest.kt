@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.groups

import com.splitit.domain.usecase.CreateGroupUseCase
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.usecase.UpdateGroupUseCase
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemoryGroupRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestClock
import com.splitit.testutils.TestIdGenerator
import com.splitit.testutils.TestIds
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.testLocalizationService
import com.splitit.testutils.group
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroupFormViewModelTest {
    @Test
    fun validatesTitleBeforeLaunchingSave() = runViewModelTest {
        val repository = InMemoryGroupRepository()
        val viewModel = createViewModel(repository)

        viewModel.save()

        assertEquals("Enter a group name.", viewModel.state.value.titleError)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun onTitleBlurSetsErrorWhenTitleBlank() = runViewModelTest {
        val repository = InMemoryGroupRepository()
        val viewModel = createViewModel(repository)

        viewModel.onTitleBlur()

        assertEquals("Enter a group name.", viewModel.state.value.titleError)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun onTitleBlurDoesNotSetErrorWhenTitleNotBlank() = runViewModelTest {
        val repository = InMemoryGroupRepository()
        val viewModel = createViewModel(repository)

        viewModel.onTitleChange("  Weekend  ")
        viewModel.onTitleBlur()

        assertEquals(null, viewModel.state.value.titleError)
    }

    @Test
    fun createsGroupAndExposesSavedId() = runViewModelTest {
        val repository = InMemoryGroupRepository()
        val viewModel = createViewModel(repository)

        viewModel.onTitleChange("  Weekend  ")
        viewModel.onDescriptionChange("  Notes  ")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(TestIds.group, viewModel.state.value.savedGroupId)
        assertFalse(viewModel.state.value.isSaving)
        assertEquals("Weekend", repository.savedGroups.single().title)
        assertEquals("Notes", repository.savedGroups.single().description)

        viewModel.consumeSavedGroup()
        assertEquals(null, viewModel.state.value.savedGroupId)
    }

    @Test
    fun loadsAndUpdatesExistingGroup() = runViewModelTest {
        val original = group(title = "Old title", description = "Old description")
        val repository = InMemoryGroupRepository(listOf(original))
        val viewModel = createViewModel(repository, original.id)
        advanceUntilIdle()

        assertEquals("Old title", viewModel.state.value.title)
        assertEquals("Old description", viewModel.state.value.description)

        viewModel.onTitleChange("New title")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(original.id, viewModel.state.value.savedGroupId)
        assertEquals("New title", repository.savedGroups.single().title)
    }

    private fun createViewModel(
        groupRepository: InMemoryGroupRepository,
        groupId: com.splitit.domain.value.GroupId? = null,
    ): GroupFormViewModel {
        return GroupFormViewModel(
            groupId = groupId,
            createGroup = CreateGroupUseCase(
                groupRepository,
                TestIdGenerator(),
                TestClock(20L),
            ),
            updateGroup = UpdateGroupUseCase(groupRepository, TestClock(20L)),
            observeGroupDetails = ObserveGroupDetailsUseCase(
                groupRepository = groupRepository,
                participantRepository = InMemoryParticipantRepository(),
                expenseRepository = InMemoryExpenseRepository(),
                settlementRepository = InMemorySettlementRepository(),
            ),
            localization = testLocalizationService,
        )
    }
}
