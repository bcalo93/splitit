package com.splitit.domain.usecase

import com.splitit.domain.model.Balance
import com.splitit.domain.model.Debt
import com.splitit.domain.model.Settlement
import com.splitit.domain.model.SettlementTransfer
import com.splitit.domain.optimizer.Optimizer
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.repository.SettlementRepository
import com.splitit.domain.service.BalanceCalculator
import com.splitit.domain.service.SourceRevisionCalculator
import com.splitit.domain.value.Clock
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SettlementId

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
    private val optimizer: Optimizer<Debt>,
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
            transfers = optimize(settlementId, debts),
        )

        settlementRepository.saveSettlement(settlement)
        return settlement
    }

    private fun optimize(settlementId: SettlementId, debts: List<Debt>): List<SettlementTransfer> {
        if (debts.isEmpty()) return emptyList()

        val currencyCode = debts.first().amount.currencyCode
        require(debts.all { it.amount.currencyCode == currencyCode }) {
            "All debts must use the same currency."
        }

        val optimized = optimizer.optimize(debts.toSet()).elements

        val transfers = optimized.map { debt ->
            SettlementTransfer(
                id = idGenerator.newTransferId(),
                settlementId = settlementId,
                fromParticipantId = debt.fromParticipantId,
                toParticipantId = debt.toParticipantId,
                amount = debt.amount,
            )
        }.sortedWith(
            compareBy<SettlementTransfer> { it.fromParticipantId.value }
                .thenBy { it.toParticipantId.value }
                .thenBy { it.amount.minorUnits },
        )

        require(debtNetAmounts(debts) == transferNetAmounts(transfers)) {
            "The optimizer returned transfers that do not settle the input debts."
        }
        return transfers
    }

    private fun debtNetAmounts(debts: List<Debt>): Map<ParticipantId, Long> {
        val amounts = mutableMapOf<ParticipantId, Long>()
        debts.forEach { debt ->
            amounts[debt.fromParticipantId] =
                (amounts[debt.fromParticipantId] ?: 0L) - debt.amount.minorUnits
            amounts[debt.toParticipantId] =
                (amounts[debt.toParticipantId] ?: 0L) + debt.amount.minorUnits
        }
        return amounts.filterValues { it != 0L }
    }

    private fun transferNetAmounts(transfers: List<SettlementTransfer>): Map<ParticipantId, Long> {
        val amounts = mutableMapOf<ParticipantId, Long>()
        transfers.forEach { transfer ->
            amounts[transfer.fromParticipantId] =
                (amounts[transfer.fromParticipantId] ?: 0L) - transfer.amount.minorUnits
            amounts[transfer.toParticipantId] =
                (amounts[transfer.toParticipantId] ?: 0L) + transfer.amount.minorUnits
        }
        return amounts.filterValues { it != 0L }
    }
}
