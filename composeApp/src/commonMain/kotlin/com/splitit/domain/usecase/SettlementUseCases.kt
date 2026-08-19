package com.splitit.domain.usecase

import com.splitit.domain.model.Balance
import com.splitit.domain.model.Settlement
import com.splitit.domain.optimizer.PaymentOptimizerAdapter
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.repository.SettlementRepository
import com.splitit.domain.service.BalanceCalculator
import com.splitit.domain.service.SourceRevisionCalculator
import com.splitit.domain.value.Clock
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.GroupId

class CalculateGroupBalancesUseCase(
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val balanceCalculator: BalanceCalculator,
) {
    suspend operator fun invoke(groupId: GroupId): List<Balance> {
        return balanceCalculator.calculateBalances(
            participants = participantRepository.getParticipants(groupId),
            expenses = expenseRepository.getExpenses(groupId),
        )
    }
}

class GenerateSettlementUseCase(
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val balanceCalculator: BalanceCalculator,
    private val optimizerAdapter: PaymentOptimizerAdapter,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {
    suspend operator fun invoke(groupId: GroupId): Settlement {
        val participants = participantRepository.getParticipants(groupId)
        val expenses = expenseRepository.getExpenses(groupId)
        val balances = balanceCalculator.calculateBalances(participants, expenses)
        val debts = balanceCalculator.calculateDebts(balances)
        val settlementId = idGenerator.newSettlementId()

        val settlement = Settlement(
            id = settlementId,
            groupId = groupId,
            generatedAtMillis = clock.nowMillis(),
            sourceRevision = SourceRevisionCalculator.calculate(participants, expenses),
            transfers = optimizerAdapter.optimize(settlementId, debts),
        )

        settlementRepository.saveSettlement(settlement)
        return settlement
    }
}
