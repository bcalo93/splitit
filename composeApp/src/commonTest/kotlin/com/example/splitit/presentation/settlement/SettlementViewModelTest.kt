@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.splitit.presentation.settlement

import com.example.splitit.domain.optimizer.PaymentOptimizerAdapter
import com.example.splitit.domain.service.BalanceCalculator
import com.example.splitit.domain.usecase.CalculateSessionBalancesUseCase
import com.example.splitit.domain.usecase.GenerateSettlementUseCase
import com.example.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.example.splitit.logic.optimizers.ComposedOptimizer
import com.example.splitit.logic.optimizers.debt.CycleOptimizer
import com.example.splitit.logic.optimizers.debt.TransitiveOptimizer
import com.example.splitit.testutils.InMemoryExpenseRepository
import com.example.splitit.testutils.InMemoryParticipantRepository
import com.example.splitit.testutils.InMemorySessionRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SettlementViewModelTest {
    @Test
    fun generatesSettlementAndMarksItStaleAfterSourceChanges() = runViewModelTest {
        val participantRepository = InMemoryParticipantRepository(
            listOf(participant(TestIds.alice), participant(TestIds.bob)),
        )
        val expenseRepository = InMemoryExpenseRepository(listOf(expense()))
        val settlementRepository = InMemorySettlementRepository()
        val viewModel = createViewModel(
            participantRepository = participantRepository,
            expenseRepository = expenseRepository,
            settlementRepository = settlementRepository,
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.canGenerateSettlement)
        assertEquals(null, viewModel.state.value.settlement)

        viewModel.generate()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.settlement)
        assertFalse(viewModel.state.value.isSettlementStale)
        assertEquals(1, viewModel.state.value.settlement?.transfers?.size)

        expenseRepository.saveExpense(expense().copy(title = "Updated dinner", updatedAtMillis = 2L))
        viewModel.refresh()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isSettlementStale)
    }

    @Test
    fun blocksGenerationWhenThereAreNotEnoughParticipantsOrExpenses() = runViewModelTest {
        val viewModel = createViewModel(
            participantRepository = InMemoryParticipantRepository(listOf(participant())),
            expenseRepository = InMemoryExpenseRepository(),
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.canGenerateSettlement)
        viewModel.generate()

        assertEquals("Add at least two participants and an expense first.", viewModel.state.value.errorMessage)
    }

    private fun createViewModel(
        participantRepository: InMemoryParticipantRepository,
        expenseRepository: InMemoryExpenseRepository,
        settlementRepository: InMemorySettlementRepository = InMemorySettlementRepository(),
    ): SettlementViewModel {
        val sessionRepository = InMemorySessionRepository(listOf(session()))
        val balanceCalculator = BalanceCalculator()
        return SettlementViewModel(
            sessionId = TestIds.session,
            observeSessionDetails = ObserveSessionDetailsUseCase(
                sessionRepository,
                participantRepository,
                expenseRepository,
                settlementRepository,
            ),
            calculateSessionBalances = CalculateSessionBalancesUseCase(
                participantRepository,
                expenseRepository,
                balanceCalculator,
            ),
            generateSettlement = GenerateSettlementUseCase(
                participantRepository = participantRepository,
                expenseRepository = expenseRepository,
                settlementRepository = settlementRepository,
                balanceCalculator = balanceCalculator,
                optimizerAdapter = PaymentOptimizerAdapter(
                    optimizer = ComposedOptimizer(listOf(CycleOptimizer(), TransitiveOptimizer())),
                    idGenerator = TestIdGenerator(),
                ),
                idGenerator = TestIdGenerator(),
                clock = TestClock(20L),
            ),
        )
    }
}
