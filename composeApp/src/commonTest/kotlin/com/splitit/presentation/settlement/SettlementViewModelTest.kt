@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.settlement

import com.splitit.domain.optimizer.PaymentOptimizerAdapter
import com.splitit.domain.service.BalanceCalculator
import com.splitit.domain.usecase.CalculateSessionBalancesUseCase
import com.splitit.domain.usecase.GenerateSettlementUseCase
import com.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.splitit.logic.optimizers.ComposedOptimizer
import com.splitit.logic.optimizers.debt.CycleOptimizer
import com.splitit.logic.optimizers.debt.TransitiveOptimizer
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemorySessionRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestClock
import com.splitit.testutils.TestIdGenerator
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.session
import com.splitit.testutils.testLocalizationService
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
            localization = testLocalizationService,
        )
    }
}
