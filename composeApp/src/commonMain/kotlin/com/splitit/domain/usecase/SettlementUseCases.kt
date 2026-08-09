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
import com.splitit.domain.value.SessionId

class CalculateSessionBalancesUseCase(
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val balanceCalculator: BalanceCalculator,
) {
    suspend operator fun invoke(sessionId: SessionId): List<Balance> {
        return balanceCalculator.calculateBalances(
            participants = participantRepository.getParticipants(sessionId),
            expenses = expenseRepository.getExpenses(sessionId),
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
    suspend operator fun invoke(sessionId: SessionId): Settlement {
        val participants = participantRepository.getParticipants(sessionId)
        val expenses = expenseRepository.getExpenses(sessionId)
        val balances = balanceCalculator.calculateBalances(participants, expenses)
        val debts = balanceCalculator.calculateDebts(balances)
        val settlementId = idGenerator.newSettlementId()

        val settlement = Settlement(
            id = settlementId,
            sessionId = sessionId,
            generatedAtMillis = clock.nowMillis(),
            sourceRevision = SourceRevisionCalculator.calculate(participants, expenses),
            transfers = optimizerAdapter.optimize(settlementId, debts),
        )

        settlementRepository.saveSettlement(settlement)
        return settlement
    }
}
