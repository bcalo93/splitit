@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.groupdetail

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.model.Settlement
import com.splitit.domain.usecase.GroupDetails
import com.splitit.domain.usecase.ObserveGroupDetailsParams
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
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

class GroupDetailsViewModelTest {
    @Test
    fun loadsDetailsAndCanRefresh() = runViewModelTest {
        val observeGroupDetails = mockk<ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails(any()) } returns groupDetailsFixture()

        val viewModel = GroupDetailsViewModel(
            groupId = TestIds.group,
            observeGroupDetails = observeGroupDetails,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(TestIds.group, viewModel.state.value.details?.group?.id)
        assertEquals(1, viewModel.state.value.details?.participants?.size)
        coVerify(exactly = 1) { observeGroupDetails(ObserveGroupDetailsParams(TestIds.group)) }

        viewModel.refresh()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
        coVerify(exactly = 2) { observeGroupDetails(ObserveGroupDetailsParams(TestIds.group)) }
    }

    @Test
    fun aggregatesTotalSpentAcrossExpenses() = runViewModelTest {
        val observeGroupDetails = mockk<ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails(any()) } returns groupDetailsFixture(
            expenses = listOf(
                expense(amount = Money(1_000L, "USD")),
                expense(id = TestIds.secondExpense, amount = Money(2_500L, "USD")),
            ),
        )

        val viewModel = GroupDetailsViewModel(
            groupId = TestIds.group,
            observeGroupDetails = observeGroupDetails,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertEquals(Money(3_500L, "USD"), viewModel.state.value.totalSpent)
    }

    @Test
    fun exposesNullTotalSpentWithoutExpenses() = runViewModelTest {
        val observeGroupDetails = mockk<ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails(any()) } returns groupDetailsFixture(expenses = emptyList())

        val viewModel = GroupDetailsViewModel(
            groupId = TestIds.group,
            observeGroupDetails = observeGroupDetails,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertNull(viewModel.state.value.totalSpent)
    }

    @Test
    fun exposesMissingGroupError() = runViewModelTest {
        val observeGroupDetails = mockk<ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails(any()) } throws IllegalArgumentException("Group ${TestIds.group.value} was not found.")

        val viewModel = GroupDetailsViewModel(
            groupId = TestIds.group,
            observeGroupDetails = observeGroupDetails,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Group group was not found.", viewModel.state.value.errorMessage)
        coVerify(exactly = 1) { observeGroupDetails(ObserveGroupDetailsParams(TestIds.group)) }
    }

    private fun groupDetailsFixture(
        group: ExpenseGroup = group(),
        participants: List<Participant> = listOf(participant()),
        expenses: List<Expense> = emptyList(),
        latestSettlement: Settlement? = null,
    ): GroupDetails = GroupDetails(
        group = group,
        participants = participants,
        expenses = expenses,
        latestSettlement = latestSettlement,
    )
}
