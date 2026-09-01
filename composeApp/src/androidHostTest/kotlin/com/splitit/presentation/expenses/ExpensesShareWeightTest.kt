@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.expenses

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.repository.AppSettings
import com.splitit.domain.usecase.GroupDetails
import com.splitit.domain.value.Money
import com.splitit.testutils.TestClock
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
import kotlin.test.assertTrue

class ExpensesShareAmountTest {
    @Test
    fun byAmountModePersistsAmountsOnSave() = runViewModelTest {
        val createExpense = mockk<com.splitit.domain.usecase.CreateExpenseUseCase>()
        coEvery { createExpense.invoke(any()) } returns expense(
            amount = Money(1_000L, "EUR"),
            payerId = TestIds.alice,
            participantIds = listOf(TestIds.alice, TestIds.bob, TestIds.charlie),
            shareAmounts = mapOf(
                TestIds.alice to 300L,
                TestIds.bob to 400L,
                TestIds.charlie to 300L,
            ),
        )

        val viewModel = createViewModel(createExpense = createExpense)
        advanceUntilIdle()

        viewModel.onTitleChange("Dinner")
        viewModel.onAmountChange("10.00")
        viewModel.selectAllParticipants()
        viewModel.onSplitModeChanged(SplitMode.ByAmount)
        viewModel.onShareAmountChanged(TestIds.alice, "3.00")
        viewModel.onShareAmountChanged(TestIds.bob, "4.00")
        viewModel.onShareAmountChanged(TestIds.charlie, "3.00")
        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            createExpense.invoke(
                match {
                    it.shareAmounts[TestIds.alice] == 300L &&
                        it.shareAmounts[TestIds.bob] == 400L &&
                        it.shareAmounts[TestIds.charlie] == 300L &&
                        it.participantIds.size == 3
                },
            )
        }
    }

    @Test
    fun equalModePersistsEqualAmountsOnSave() = runViewModelTest {
        val createExpense = mockk<com.splitit.domain.usecase.CreateExpenseUseCase>()
        coEvery { createExpense.invoke(any()) } returns expense(
            amount = Money(1_200L, "EUR"),
            participantIds = listOf(TestIds.alice, TestIds.bob),
        )

        val viewModel = createViewModel(createExpense = createExpense)
        advanceUntilIdle()

        viewModel.onTitleChange("Lunch")
        viewModel.onAmountChange("12.00")
        viewModel.onParticipantToggled(TestIds.bob)
        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            createExpense.invoke(
                match {
                    it.participantIds.size == 2 && it.shareAmounts.values.all { amount -> amount == 600L }
                },
            )
        }
    }

    @Test
    fun byAmountModeShowsErrorWhenSumExceedsTotal() = runViewModelTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAmountChange("10.00")
        viewModel.selectAllParticipants()
        viewModel.onSplitModeChanged(SplitMode.ByAmount)
        viewModel.onShareAmountChanged(TestIds.alice, "5.00")
        viewModel.onShareAmountChanged(TestIds.bob, "5.00")
        viewModel.onShareAmountChanged(TestIds.charlie, "1.00")

        val state = viewModel.state.value
        assertEquals(-100L, state.shareAmountsRemainder)
    }

    @Test
    fun byAmountModeShowsRemainder() = runViewModelTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAmountChange("10.00")
        viewModel.selectAllParticipants()
        viewModel.onSplitModeChanged(SplitMode.ByAmount)
        viewModel.onShareAmountChanged(TestIds.alice, "3.00")

        val state = viewModel.state.value
        assertEquals(700L, state.shareAmountsRemainder)
    }

    private fun createViewModel(
        createExpense: com.splitit.domain.usecase.CreateExpenseUseCase = mockk<com.splitit.domain.usecase.CreateExpenseUseCase>(),
    ): ExpensesViewModel {
        val participants = listOf(
            participant(TestIds.alice),
            participant(TestIds.bob),
            participant(TestIds.charlie),
        )
        val observeGroupDetails = mockk<com.splitit.domain.usecase.ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails.invoke(any()) } returns groupDetails(participants = participants)
        return ExpensesViewModel(
            groupId = TestIds.group,
            observeGroupDetails = observeGroupDetails,
            createExpense = createExpense,
            updateExpense = mockk<com.splitit.domain.usecase.UpdateExpenseUseCase>(),
            deleteExpense = mockk<com.splitit.domain.usecase.DeleteExpenseUseCase>(),
            clock = TestClock(20L),
            getSettings = stubGetSettings(),
            localization = testLocalizationService,
        )
    }

    private fun stubGetSettings(): com.splitit.domain.usecase.GetSettingsUseCase {
        val mock = mockk<com.splitit.domain.usecase.GetSettingsUseCase>()
        coEvery { mock.invoke(any()) } returns AppSettings(defaultCurrencyCode = "EUR")
        return mock
    }

    private fun groupDetails(
        group: ExpenseGroup = group(),
        participants: List<Participant>,
        expenses: List<Expense> = emptyList(),
    ): GroupDetails = GroupDetails(
        group = group,
        participants = participants,
        expenses = expenses,
        latestSettlement = null,
    )
}
