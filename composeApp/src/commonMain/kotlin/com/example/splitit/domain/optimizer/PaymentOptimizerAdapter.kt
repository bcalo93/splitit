package com.example.splitit.domain.optimizer

import com.example.splitit.domain.model.Debt
import com.example.splitit.domain.model.SettlementTransfer
import com.example.splitit.domain.value.IdGenerator
import com.example.splitit.domain.value.Money
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SettlementId
import com.example.splitit.logic.optimizers.Optimizer
import com.example.splitit.domain.Participant as OptimizerParticipant
import com.example.splitit.domain.Payment as OptimizerPayment

class PaymentOptimizerAdapter(
    private val optimizer: Optimizer<OptimizerPayment>,
    private val idGenerator: IdGenerator,
) {
    fun optimize(
        settlementId: SettlementId,
        debts: List<Debt>,
    ): List<SettlementTransfer> {
        if (debts.isEmpty()) return emptyList()

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

        return optimized.map { payment ->
            SettlementTransfer(
                id = idGenerator.newTransferId(),
                settlementId = settlementId,
                fromParticipantId = ParticipantId(payment.from.nickname),
                toParticipantId = ParticipantId(payment.to.nickname),
                amount = Money(payment.amount.toLong(), debts.first().amount.currencyCode),
            )
        }
    }

    private fun Money.toOptimizerAmount(): Int {
        require(minorUnits <= Int.MAX_VALUE) {
            "Debt amount $minorUnits is too large for the current optimizer contract."
        }
        require(minorUnits > 0) { "Debt amount must be positive." }
        return minorUnits.toInt()
    }
}
