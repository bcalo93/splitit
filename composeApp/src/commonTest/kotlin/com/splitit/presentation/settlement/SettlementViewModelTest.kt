@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.settlement

import com.splitit.domain.optimizer.ComposedOptimizer
import com.splitit.domain.optimizer.CycleOptimizer
import com.splitit.domain.optimizer.TransitiveOptimizer
import com.splitit.domain.service.BalanceCalculator
import com.splitit.domain.usecase.CalculateGroupBalancesUseCase
import com.splitit.domain.usecase.GenerateSettlementUseCase
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.usecase.RecordTransferPaymentUseCase
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemoryGroupRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SettlementViewModelTest {
    @Test
    fun calculatesSettlementOnLoadAndRefreshesItAfterSourceChanges() = runViewModelTest {
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
        assertNotNull(viewModel.state.value.settlement)
        assertFalse(viewModel.state.value.isSettlementStale)
        assertEquals(1, viewModel.state.value.settlement?.transfers?.size)

        expenseRepository.saveExpense(expense().copy(title = "Updated dinner", updatedAtMillis = 2L))
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSettlementStale)
        assertEquals(2, settlementRepository.saveCalls)
    }

    @Test
    fun recordsTransferPaymentAndRegeneratesSettlement() = runViewModelTest {
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

        val transfer = viewModel.state.value.settlement?.transfers?.first()
        assertNotNull(transfer)
        assertEquals(1, viewModel.state.value.settlement?.transfers?.size)

        viewModel.recordTransferPayment(transfer)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.settlement?.transfers.isNullOrEmpty())
        assertEquals(2, settlementRepository.saveCalls)
        assertEquals(1, expenseRepository.saveCalls)
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
        val groupRepository = InMemoryGroupRepository(listOf(group()))
        val balanceCalculator = BalanceCalculator()
        val idGenerator = TestIdGenerator(expenseId = TestIds.secondExpense)
        val clock = TestClock(20L)
        return SettlementViewModel(
            groupId = TestIds.group,
            observeGroupDetails = ObserveGroupDetailsUseCase(
                groupRepository,
                participantRepository,
                expenseRepository,
                settlementRepository,
            ),
            calculateGroupBalances = CalculateGroupBalancesUseCase(
                participantRepository,
                expenseRepository,
                balanceCalculator,
            ),
            generateSettlement = GenerateSettlementUseCase(
                participantRepository = participantRepository,
                expenseRepository = expenseRepository,
                settlementRepository = settlementRepository,
                balanceCalculator = balanceCalculator,
                optimizer = ComposedOptimizer(listOf(CycleOptimizer(), TransitiveOptimizer())),
                idGenerator = idGenerator,
                clock = clock,
            ),
            recordTransferPaymentUseCase = RecordTransferPaymentUseCase(
                groupRepository = groupRepository,
                participantRepository = participantRepository,
                expenseRepository = expenseRepository,
                idGenerator = idGenerator,
                clock = clock,
                localization = testLocalizationService,
            ),
            localization = testLocalizationService,
        )
    }
}
