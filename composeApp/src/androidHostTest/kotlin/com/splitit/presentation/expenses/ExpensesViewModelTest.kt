@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.expenses

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.repository.AppSettings
import com.splitit.domain.usecase.DeleteExpenseParams
import com.splitit.domain.usecase.GroupDetails
import com.splitit.domain.value.Money
import com.splitit.testutils.TestIds
import com.splitit.testutils.TestClock
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
import kotlin.test.assertNull

class ExpensesViewModelTest {
    @Test
    fun loadsParticipantsWithDefaultPayerAndSelection() = runViewModelTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(TestIds.alice, viewModel.state.value.payerId)
        assertEquals(setOf(TestIds.alice), viewModel.state.value.selectedParticipantIds)
        assertEquals("EUR", viewModel.state.value.defaultCurrencyCode)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun validatesRequiredExpenseFieldsBeforeSaving() = runViewModelTest {
        val createExpense = mockk<com.splitit.domain.usecase.CreateExpenseUseCase>()
        val viewModel = createViewModel(createExpense = createExpense)
        advanceUntilIdle()

        viewModel.save()

        assertEquals("Enter an expense title.", viewModel.state.value.titleError)
        assertEquals("Enter a positive amount.", viewModel.state.value.amountError)
        coVerify(exactly = 0) { createExpense.invoke(any()) }
    }

    @Test
    fun createsExpenseUsingDefaultCurrencyAndNormalizedFormValues() = runViewModelTest {
        val createExpense = mockk<com.splitit.domain.usecase.CreateExpenseUseCase>()
        coEvery { createExpense.invoke(any()) } returns expense(
            title = "Dinner",
            amount = Money(1_250L, "EUR"),
            payerId = TestIds.alice,
            participantIds = listOf(TestIds.alice, TestIds.bob),
        )

        val viewModel = createViewModel(createExpense = createExpense)
        advanceUntilIdle()

        viewModel.onTitleChange("  Dinner  ")
        viewModel.onAmountChange("12.50")
        viewModel.onNoteChange("  With dessert  ")
        viewModel.onParticipantToggled(TestIds.bob)
        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 1) { createExpense.invoke(any()) }
        assertNull(viewModel.state.value.editingExpenseId)
    }

    @Test
    fun editingExpensePreservesHistoricalCurrency() = runViewModelTest {
        val original = expense(amount = Money(1_500L, "USD"))
        val updateExpense = mockk<com.splitit.domain.usecase.UpdateExpenseUseCase>()
        coEvery { updateExpense.invoke(any()) } returns original.copy(amount = Money(2_000L, "USD"))

        val viewModel = createViewModel(
            initialDetails = groupDetails(expenses = listOf(original)),
            updateExpense = updateExpense,
        )
        advanceUntilIdle()

        viewModel.startEditing(original)
        viewModel.onSplitModeChanged(SplitMode.Equal)
        viewModel.onAmountChange("20")
        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            updateExpense.invoke(match { it.amount == Money(2_000L, "USD") })
        }
    }

    @Test
    fun deletingTheExpenseBeingEditedClearsTheForm() = runViewModelTest {
        val original = expense()
        val deleteExpense = mockk<com.splitit.domain.usecase.DeleteExpenseUseCase>()
        coEvery { deleteExpense.invoke(any()) } returns Unit

        val viewModel = createViewModel(
            initialDetails = groupDetails(expenses = listOf(original)),
            deleteExpense = deleteExpense,
        )
        advanceUntilIdle()

        viewModel.startEditing(original)
        viewModel.delete(original.id)
        advanceUntilIdle()

        assertNull(viewModel.state.value.editingExpenseId)
        coVerify(exactly = 1) { deleteExpense.invoke(DeleteExpenseParams(original.id)) }
    }

    @Test
    fun filtersExpensesByTitleAndNote() = runViewModelTest {
        val firstExpense = expense(title = "Dinner")
        val secondExpense = expense(
            id = TestIds.secondExpense,
            title = "Hotel",
            note = "Beach weekend",
        )
        val viewModel = createViewModel(
            initialDetails = groupDetails(expenses = listOf(firstExpense, secondExpense)),
        )
        advanceUntilIdle()

        viewModel.onSearchQueryChange("hotel")
        assertEquals(listOf(TestIds.secondExpense), viewModel.state.value.visibleExpenses.map { it.id })

        viewModel.onSearchQueryChange("WEEKEND")
        assertEquals(listOf(TestIds.secondExpense), viewModel.state.value.visibleExpenses.map { it.id })

        viewModel.onSearchQueryChange("")
        assertEquals(
            listOf(firstExpense.id, secondExpense.id),
            viewModel.state.value.visibleExpenses.map { it.id },
        )
    }

    private fun createViewModel(
        initialDetails: GroupDetails = groupDetails(),
        createExpense: com.splitit.domain.usecase.CreateExpenseUseCase = mockk<com.splitit.domain.usecase.CreateExpenseUseCase>(),
        updateExpense: com.splitit.domain.usecase.UpdateExpenseUseCase = mockk<com.splitit.domain.usecase.UpdateExpenseUseCase>(),
        deleteExpense: com.splitit.domain.usecase.DeleteExpenseUseCase = mockk<com.splitit.domain.usecase.DeleteExpenseUseCase>(),
        getSettings: com.splitit.domain.usecase.GetSettingsUseCase = stubGetSettings(),
    ): ExpensesViewModel {
        val observeGroupDetails = mockk<com.splitit.domain.usecase.ObserveGroupDetailsUseCase>()
        coEvery { observeGroupDetails.invoke(any()) } returns initialDetails
        return ExpensesViewModel(
            groupId = TestIds.group,
            observeGroupDetails = observeGroupDetails,
            createExpense = createExpense,
            updateExpense = updateExpense,
            deleteExpense = deleteExpense,
            clock = TestClock(99L),
            getSettings = getSettings,
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
        participants: List<Participant> = listOf(participant(TestIds.alice), participant(TestIds.bob)),
        expenses: List<Expense> = emptyList(),
    ): GroupDetails = GroupDetails(
        group = group,
        participants = participants,
        expenses = expenses,
        latestSettlement = null,
    )
}
