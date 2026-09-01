@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.groups

import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.usecase.DeleteGroupParams
import com.splitit.domain.usecase.GroupDetails
import com.splitit.domain.value.Money
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
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

class GroupListViewModelTest {
    @Test
    fun loadsEmptyStateAfterInitialRefresh() = runViewModelTest {
        val observeGroups = mockk<com.splitit.domain.usecase.ObserveGroupsUseCase>()
        coEvery { observeGroups.invoke(any()) } returns emptyList()
        val observeGroupDetails = mockk<com.splitit.domain.usecase.ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails.invoke(any()) } returns emptyGroupDetails()
        val deleteGroup = mockk<com.splitit.domain.usecase.DeleteGroupUseCase>()

        val viewModel = GroupListViewModel(
            observeGroups = observeGroups,
            observeGroupDetails = observeGroupDetails,
            deleteGroup = deleteGroup,
            localization = testLocalizationService,
        )

        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.groups.isEmpty())
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun exposesLoadErrors() = runViewModelTest {
        val observeGroups = mockk<com.splitit.domain.usecase.ObserveGroupsUseCase>()
        coEvery { observeGroups.invoke(any()) } throws IllegalStateException("database unavailable")

        val viewModel = GroupListViewModel(
            observeGroups = observeGroups,
            observeGroupDetails = mockk<com.splitit.domain.usecase.ObserveGroupDetailsUseCase>(),
            deleteGroup = mockk<com.splitit.domain.usecase.DeleteGroupUseCase>(),
            localization = testLocalizationService,
        )

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("database unavailable", viewModel.state.value.errorMessage)
    }

    @Test
    fun deletesGroupAndRefreshesList() = runViewModelTest {
        val observeGroups = mockk<com.splitit.domain.usecase.ObserveGroupsUseCase>()
        coEvery { observeGroups.invoke(any()) } returnsMany listOf(listOf(group()), emptyList())
        val observeGroupDetails = mockk<com.splitit.domain.usecase.ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails.invoke(any()) } returns emptyGroupDetails()
        val deleteGroup = mockk<com.splitit.domain.usecase.DeleteGroupUseCase>()
        coEvery { deleteGroup.invoke(any()) } returns Unit

        val viewModel = GroupListViewModel(
            observeGroups = observeGroups,
            observeGroupDetails = observeGroupDetails,
            deleteGroup = deleteGroup,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        viewModel.delete(TestIds.group)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.groups.isEmpty())
        coVerify(exactly = 1) { deleteGroup.invoke(DeleteGroupParams(TestIds.group)) }
    }

    @Test
    fun filtersGroupsByTitleAndDescription() = runViewModelTest {
        val groups = listOf(
            group(title = "Beach trip"),
            group(
                id = TestIds.secondGroup,
                title = "Monthly bills",
                description = "Shared home expenses",
            ),
        )
        val observeGroups = mockk<com.splitit.domain.usecase.ObserveGroupsUseCase>()
        coEvery { observeGroups.invoke(any()) } returns groups

        val observeGroupDetails = mockk<com.splitit.domain.usecase.ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails.invoke(any()) } returns emptyGroupDetails()

        val viewModel = GroupListViewModel(
            observeGroups = observeGroups,
            observeGroupDetails = observeGroupDetails,
            deleteGroup = mockk<com.splitit.domain.usecase.DeleteGroupUseCase>(),
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        viewModel.onSearchQueryChange("BEACH")
        assertEquals(listOf(TestIds.group), viewModel.state.value.visibleGroups.map { it.id })

        viewModel.onSearchQueryChange("home")
        assertEquals(listOf(TestIds.secondGroup), viewModel.state.value.visibleGroups.map { it.id })

        viewModel.onSearchQueryChange("missing")
        assertTrue(viewModel.state.value.visibleGroups.isEmpty())
    }

    @Test
    fun marksGroupPendingWhenItHasUnsettledExpenses() = runViewModelTest {
        val alice = participant(TestIds.alice)
        val expenseFixture = expense(amount = Money(2_000L, "USD"))
        val observeGroups = mockk<com.splitit.domain.usecase.ObserveGroupsUseCase>()
        coEvery { observeGroups.invoke(any()) } returns listOf(group())
        val observeGroupDetails = mockk<com.splitit.domain.usecase.ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails.invoke(any()) } returns GroupDetails(
            group = group(),
            participants = listOf(alice),
            expenses = listOf(expenseFixture),
            latestSettlement = null,
        )

        val viewModel = GroupListViewModel(
            observeGroups = observeGroups,
            observeGroupDetails = observeGroupDetails,
            deleteGroup = mockk<com.splitit.domain.usecase.DeleteGroupUseCase>(),
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertEquals(setOf(TestIds.group), viewModel.state.value.pendingGroupIds)
        assertEquals(listOf(TestIds.alice), viewModel.state.value.participantsByGroup[TestIds.group]?.map { it.id })
    }

    private fun emptyGroupDetails(): GroupDetails = GroupDetails(
        group = group(),
        participants = emptyList<Participant>(),
        expenses = emptyList(),
        latestSettlement = null,
    )
}
