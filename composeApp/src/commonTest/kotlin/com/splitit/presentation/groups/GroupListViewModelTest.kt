@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.groups

import com.splitit.domain.usecase.DeleteGroupUseCase
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.usecase.ObserveGroupsUseCase
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemoryGroupRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.group
import com.splitit.testutils.testLocalizationService
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroupListViewModelTest {
    @Test
    fun loadsEmptyStateAfterInitialRefresh() = runViewModelTest {
        val repository = InMemoryGroupRepository()
        val viewModel = viewModel(repository)

        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.groups.isEmpty())
        assertEquals(null, viewModel.state.value.errorMessage)
    }

    @Test
    fun exposesLoadErrors() = runViewModelTest {
        val repository = InMemoryGroupRepository()
        repository.getError = IllegalStateException("database unavailable")
        val viewModel = viewModel(repository)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("database unavailable", viewModel.state.value.errorMessage)
    }

    @Test
    fun deletesGroupAndRefreshesList() = runViewModelTest {
        val repository = InMemoryGroupRepository(listOf(group()))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.delete(TestIds.group)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.groups.isEmpty())
        assertEquals(1, repository.deleteCalls)
    }

    @Test
    fun filtersGroupsByTitleAndDescription() = runViewModelTest {
        val repository = InMemoryGroupRepository(
            listOf(
                group(title = "Beach trip"),
                group(
                    id = TestIds.secondGroup,
                    title = "Monthly bills",
                    description = "Shared home expenses",
                ),
            ),
        )
        val viewModel = viewModel(repository)
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
        val groupRepository = InMemoryGroupRepository(
            listOf(
                group(
                    participantIds = setOf(TestIds.alice),
                    expenseIds = setOf(TestIds.expense),
                ),
            ),
        )
        val participantRepository = InMemoryParticipantRepository(listOf(participant()))
        val expenseRepository = InMemoryExpenseRepository(listOf(expense()))
        val viewModel = GroupListViewModel(
            observeGroups = ObserveGroupsUseCase(groupRepository),
            observeGroupDetails = ObserveGroupDetailsUseCase(
                groupRepository,
                participantRepository,
                expenseRepository,
                InMemorySettlementRepository(),
            ),
            deleteGroup = DeleteGroupUseCase(groupRepository),
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertEquals(setOf(TestIds.group), viewModel.state.value.pendingGroupIds)
        assertEquals(
            listOf(TestIds.alice),
            viewModel.state.value.participantsByGroup[TestIds.group]?.map { it.id },
        )
    }

    private fun viewModel(repository: InMemoryGroupRepository): GroupListViewModel {
        return GroupListViewModel(
            observeGroups = ObserveGroupsUseCase(repository),
            observeGroupDetails = ObserveGroupDetailsUseCase(
                repository,
                InMemoryParticipantRepository(),
                InMemoryExpenseRepository(),
                InMemorySettlementRepository(),
            ),
            deleteGroup = DeleteGroupUseCase(repository),
            localization = testLocalizationService,
        )
    }
}
