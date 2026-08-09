@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.splitit.presentation.expenses

import com.example.splitit.domain.repository.AppSettings
import com.example.splitit.domain.usecase.CreateExpenseUseCase
import com.example.splitit.domain.usecase.DeleteExpenseUseCase
import com.example.splitit.domain.usecase.GetSettingsUseCase
import com.example.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.example.splitit.domain.usecase.UpdateExpenseUseCase
import com.example.splitit.domain.value.Money
import com.example.splitit.testutils.InMemoryExpenseRepository
import com.example.splitit.testutils.InMemoryParticipantRepository
import com.example.splitit.testutils.InMemorySessionRepository
import com.example.splitit.testutils.InMemorySettingsRepository
import com.example.splitit.testutils.InMemorySettlementRepository
import com.example.splitit.testutils.TestClock
import com.example.splitit.testutils.TestIdGenerator
import com.example.splitit.testutils.TestIds
import com.example.splitit.testutils.expense
import com.example.splitit.testutils.participant
import com.example.splitit.testutils.runViewModelTest
import com.example.splitit.testutils.session
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

    private fun createViewModel(
        participantRepository: InMemoryParticipantRepository = InMemoryParticipantRepository(
            listOf(participant(TestIds.alice)),
        ),
        expenseRepository: InMemoryExpenseRepository = InMemoryExpenseRepository(),
        clock: TestClock = TestClock(20L),
    ): ExpensesViewModel {
        val sessionRepository = InMemorySessionRepository(listOf(session()))
        val settingsRepository = InMemorySettingsRepository(
            AppSettings(defaultCurrencyCode = "EUR"),
        )
        return ExpensesViewModel(
            sessionId = TestIds.session,
            observeSessionDetails = ObserveSessionDetailsUseCase(
                sessionRepository,
                participantRepository,
                expenseRepository,
                InMemorySettlementRepository(),
            ),
            createExpense = CreateExpenseUseCase(
                sessionRepository,
                participantRepository,
                expenseRepository,
                TestIdGenerator(),
                clock,
            ),
            updateExpense = UpdateExpenseUseCase(participantRepository, expenseRepository, clock),
            deleteExpense = DeleteExpenseUseCase(expenseRepository),
            clock = clock,
            getSettings = GetSettingsUseCase(settingsRepository),
        )
    }
}
