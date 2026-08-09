package com.splitit.domain.optimizer

import com.splitit.domain.model.Debt
import com.splitit.domain.model.SettlementTransfer
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SettlementId
import com.splitit.logic.optimizers.Optimizer
import com.splitit.domain.Participant as OptimizerParticipant
import com.splitit.domain.Payment as OptimizerPayment

class PaymentOptimizerAdapter(
    private val optimizer: Optimizer<OptimizerPayment>,
    private val idGenerator: IdGenerator,
) {
    fun optimize(
        settlementId: SettlementId,
        debts: List<Debt>,
    ): List<SettlementTransfer> {
        if (debts.isEmpty()) return emptyList()

        val currencyCode = debts.first().amount.currencyCode
        require(debts.all { it.amount.currencyCode == currencyCode }) {
            "All debts must use the same currency."
        }

        val participants = mutableMapOf<ParticipantId, OptimizerParticipant>()
        val optimizerPayments = debts.map { debt ->
            OptimizerPayment(
                from = participants.getOrPut(debt.fromParticipantId) {
                    OptimizerParticipant(debt.fromParticipantId.value)
                },
                to = participants.getOrPut(debt.toParticipantId) {
                    OptimizerParticipant(debt.toParticipantId.value)
                },
                amount = debt.amount.toOptimizerAmount(),
            )
        }.toSet()

        val optimized = optimizer.optimize(optimizerPayments).elements

        val transfers = optimized.map { payment ->
            require(payment.amount > 0) {
                "The optimizer returned a non-positive payment."
            }

            val fromParticipantId = ParticipantId(payment.from.nickname)
            val toParticipantId = ParticipantId(payment.to.nickname)
            require(fromParticipantId in participants && toParticipantId in participants) {
                "The optimizer returned a participant that was not in the input debts."
            }

            SettlementTransfer(
                id = idGenerator.newTransferId(),
                settlementId = settlementId,
                fromParticipantId = fromParticipantId,
                toParticipantId = toParticipantId,
                amount = Money(payment.amount.toLong(), currencyCode),
            )
        }.sortedWith(
            compareBy<SettlementTransfer> { it.fromParticipantId.value }
                .thenBy { it.toParticipantId.value }
                .thenBy { it.amount.minorUnits },
        )

        require(debtNetAmounts(debts) == transferNetAmounts(transfers)) {
            "The optimizer returned payments that do not settle the input debts."
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

    private fun Money.toOptimizerAmount(): Int {
        require(minorUnits <= Int.MAX_VALUE) {
            "Debt amount $minorUnits is too large for the current optimizer contract."
        }
        require(minorUnits > 0) { "Debt amount must be positive." }
        return minorUnits.toInt()
    }
}
