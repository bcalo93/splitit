@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.expenses

import com.splitit.domain.repository.AppSettings
import com.splitit.domain.usecase.CreateExpenseUseCase
import com.splitit.domain.usecase.DeleteExpenseUseCase
import com.splitit.domain.usecase.GetSettingsUseCase
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.usecase.UpdateExpenseUseCase
import com.splitit.domain.value.Money
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemoryGroupRepository
import com.splitit.testutils.InMemorySettingsRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestClock
import com.splitit.testutils.TestIdGenerator
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.group
import com.splitit.testutils.testLocalizationService
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpensesViewModelTest {
    @Test
    fun loadsParticipantsWithDefaultPayerAndSelection() = runViewModelTest {
        val participantRepository = InMemoryParticipantRepository(
            listOf(participant(TestIds.alice), participant(TestIds.bob)),
        )
        val viewModel = createViewModel(participantRepository = participantRepository)
        advanceUntilIdle()

        assertEquals(TestIds.alice, viewModel.state.value.payerId)
        assertEquals(setOf(TestIds.alice), viewModel.state.value.selectedParticipantIds)
        assertEquals("EUR", viewModel.state.value.defaultCurrencyCode)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun validatesRequiredExpenseFieldsBeforeSaving() = runViewModelTest {
        val expenseRepository = InMemoryExpenseRepository()
        val viewModel = createViewModel(expenseRepository = expenseRepository)
        advanceUntilIdle()

        viewModel.save()

        assertEquals("Enter an expense title.", viewModel.state.value.titleError)
        assertEquals("Enter a positive amount.", viewModel.state.value.amountError)
        assertEquals(0, expenseRepository.saveCalls)
    }

    @Test
    fun createsExpenseUsingDefaultCurrencyAndNormalizedFormValues() = runViewModelTest {
        val expenseRepository = InMemoryExpenseRepository()
        val participantRepository = InMemoryParticipantRepository(
            listOf(participant(TestIds.alice), participant(TestIds.bob)),
        )
        val viewModel = createViewModel(
            participantRepository = participantRepository,
            expenseRepository = expenseRepository,
            clock = TestClock(99L),
        )
        advanceUntilIdle()

        viewModel.onTitleChange("  Dinner  ")
        viewModel.onAmountChange("12.50")
        viewModel.onNoteChange("  With dessert  ")
        viewModel.onParticipantToggled(TestIds.bob)
        viewModel.save()
        advanceUntilIdle()

        val saved = expenseRepository.savedExpenses.single()
        assertEquals("Dinner", saved.title)
        assertEquals(Money(1_250L, "EUR"), saved.amount)
        assertEquals("With dessert", saved.note)
        assertEquals(99L, saved.dateMillis)
        assertEquals(setOf(TestIds.alice, TestIds.bob), saved.participantShares.map { it.participantId }.toSet())
        assertTrue(viewModel.state.value.editingExpenseId == null)
    }

    @Test
    fun editingExpensePreservesHistoricalCurrency() = runViewModelTest {
        val original = expense(amount = Money(1_500L, "USD"))
        val expenseRepository = InMemoryExpenseRepository(listOf(original))
        val viewModel = createViewModel(
            participantRepository = InMemoryParticipantRepository(
                listOf(participant(TestIds.alice), participant(TestIds.bob)),
            ),
            expenseRepository = expenseRepository,
        )
        advanceUntilIdle()

        viewModel.startEditing(original)
        viewModel.onAmountChange("20")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(Money(2_000L, "USD"), expenseRepository.savedExpenses.single().amount)
    }

    @Test
    fun deletingTheExpenseBeingEditedClearsTheForm() = runViewModelTest {
        val original = expense()
        val expenseRepository = InMemoryExpenseRepository(listOf(original))
        val viewModel = createViewModel(expenseRepository = expenseRepository)
        advanceUntilIdle()

        viewModel.startEditing(original)
        viewModel.delete(original.id)
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.editingExpenseId)
        assertTrue(expenseRepository.savedExpenses.isEmpty())
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
            participantRepository = InMemoryParticipantRepository(
                listOf(participant(TestIds.alice), participant(TestIds.bob)),
            ),
            expenseRepository = InMemoryExpenseRepository(listOf(firstExpense, secondExpense)),
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
        participantRepository: InMemoryParticipantRepository = InMemoryParticipantRepository(
            listOf(participant(TestIds.alice)),
        ),
        expenseRepository: InMemoryExpenseRepository = InMemoryExpenseRepository(),
        clock: TestClock = TestClock(20L),
    ): ExpensesViewModel {
        val groupRepository = InMemoryGroupRepository(listOf(group()))
        val settingsRepository = InMemorySettingsRepository(
            AppSettings(defaultCurrencyCode = "EUR"),
        )
        return ExpensesViewModel(
            groupId = TestIds.group,
            observeGroupDetails = ObserveGroupDetailsUseCase(
                groupRepository,
                participantRepository,
                expenseRepository,
                InMemorySettlementRepository(),
            ),
            createExpense = CreateExpenseUseCase(
                groupRepository,
                participantRepository,
                expenseRepository,
                TestIdGenerator(),
                clock,
            ),
            updateExpense = UpdateExpenseUseCase(participantRepository, expenseRepository, clock),
            deleteExpense = DeleteExpenseUseCase(expenseRepository),
            clock = clock,
            getSettings = GetSettingsUseCase(settingsRepository),
            localization = testLocalizationService,
        )
    }
}
