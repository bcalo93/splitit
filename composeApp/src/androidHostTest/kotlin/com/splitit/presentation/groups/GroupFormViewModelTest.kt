@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.groups

import com.splitit.domain.usecase.CreateGroupParams
import com.splitit.domain.usecase.CreateGroupUseCase
import com.splitit.domain.usecase.GroupDetails
import com.splitit.domain.usecase.ObserveGroupDetailsParams
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.usecase.UpdateGroupParams
import com.splitit.domain.usecase.UpdateGroupUseCase
import com.splitit.domain.value.GroupId
import com.splitit.testutils.TestIds
import com.splitit.testutils.group
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

class GroupFormViewModelTest {
    @Test
    fun validatesTitleBeforeLaunchingSave() = runViewModelTest {
        val createGroup = mockk<CreateGroupUseCase>()
        val viewModel = createViewModel(createGroup = createGroup)

        viewModel.save()

        assertEquals("Enter a group name.", viewModel.state.value.titleError)
        coVerify(exactly = 0) { createGroup(any()) }
    }

    @Test
    fun onTitleBlurSetsErrorWhenTitleBlank() = runViewModelTest {
        val viewModel = createViewModel()

        viewModel.onTitleBlur()

        assertEquals("Enter a group name.", viewModel.state.value.titleError)
    }

    @Test
    fun onTitleBlurDoesNotSetErrorWhenTitleNotBlank() = runViewModelTest {
        val viewModel = createViewModel()

        viewModel.onTitleChange("  Weekend  ")
        viewModel.onTitleBlur()

        assertNull(viewModel.state.value.titleError)
    }

    @Test
    fun createsGroupAndExposesSavedId() = runViewModelTest {
        val createGroup = mockk<CreateGroupUseCase>()
        coEvery { createGroup(any()) } returns group(id = TestIds.group, title = "Weekend", description = "Notes")

        val viewModel = createViewModel(createGroup = createGroup)

        viewModel.onTitleChange("  Weekend  ")
        viewModel.onDescriptionChange("  Notes  ")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(TestIds.group, viewModel.state.value.savedGroupId)
        assertFalse(viewModel.state.value.isSaving)
        coVerify(exactly = 1) {
            createGroup(CreateGroupParams(title = "  Weekend  ", description = "  Notes  "))
        }

        viewModel.consumeSavedGroup()
        assertNull(viewModel.state.value.savedGroupId)
    }

    @Test
    fun loadsAndUpdatesExistingGroup() = runViewModelTest {
        val original = group(title = "Old title", description = "Old description")
        val observeGroupDetails = mockk<ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails(any()) } returns GroupDetails(
            group = original,
            participants = emptyList(),
            expenses = emptyList(),
            latestSettlement = null,
        )
        val updateGroup = mockk<UpdateGroupUseCase>()
        coEvery { updateGroup(any()) } returns original.copy(title = "New title")

        val viewModel = createViewModel(
            observeGroupDetails = observeGroupDetails,
            updateGroup = updateGroup,
            initialGroupId = original.id,
        )
        advanceUntilIdle()

        assertEquals("Old title", viewModel.state.value.title)
        assertEquals("Old description", viewModel.state.value.description)
        coVerify(exactly = 1) { observeGroupDetails(ObserveGroupDetailsParams(original.id)) }

        viewModel.onTitleChange("New title")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(original.id, viewModel.state.value.savedGroupId)
        coVerify(exactly = 1) {
            updateGroup(UpdateGroupParams(original.id, "New title", "Old description"))
        }
    }

    private fun createViewModel(
        createGroup: CreateGroupUseCase = mockk<CreateGroupUseCase>(),
        updateGroup: UpdateGroupUseCase = mockk<UpdateGroupUseCase>(),
        observeGroupDetails: ObserveGroupDetailsUseCase = stubObserveGroupDetails(),
        initialGroupId: GroupId? = null,
    ): GroupFormViewModel {
        return GroupFormViewModel(
            groupId = initialGroupId,
            createGroup = createGroup,
            updateGroup = updateGroup,
            observeGroupDetails = observeGroupDetails,
            localization = testLocalizationService,
        )
    }

    private fun stubObserveGroupDetails(): ObserveGroupDetailsUseCase {
        val mock = mockk<ObserveGroupDetailsUseCase>()
        coEvery { mock(any()) } returns GroupDetails(
            group = group(),
            participants = emptyList(),
            expenses = emptyList(),
            latestSettlement = null,
        )
        return mock
    }
}
