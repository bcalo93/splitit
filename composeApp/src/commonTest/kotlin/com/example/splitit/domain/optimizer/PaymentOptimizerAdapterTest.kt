package com.example.splitit.domain.optimizer

import com.example.splitit.domain.model.Debt
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.IdGenerator
import com.example.splitit.domain.value.Money
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import com.example.splitit.domain.value.SettlementId
import com.example.splitit.domain.value.TransferId
import com.example.splitit.logic.optimizers.ComposedOptimizer
import com.example.splitit.logic.optimizers.debt.CycleOptimizer
import com.example.splitit.logic.optimizers.debt.TransitiveOptimizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PaymentOptimizerAdapterTest {
    private val adapter = PaymentOptimizerAdapter(
        optimizer = ComposedOptimizer(listOf(CycleOptimizer(), TransitiveOptimizer())),
        idGenerator = TestIdGenerator,
    )

    @Test
    fun mapsDebtsThroughOptimizer() {
        val settlementId = SettlementId("settlement")

        val transfers = adapter.optimize(
            settlementId = settlementId,
            debts = listOf(
                Debt(ParticipantId("alice"), ParticipantId("bob"), Money(100, "USD")),
                Debt(ParticipantId("bob"), ParticipantId("alice"), Money(40, "USD")),
            ),
        )

        assertEquals(1, transfers.size)
        assertEquals(ParticipantId("alice"), transfers.first().fromParticipantId)
        assertEquals(ParticipantId("bob"), transfers.first().toParticipantId)
        assertEquals(Money(60, "USD"), transfers.first().amount)
    }

    @Test
    fun rejectsAmountsThatDoNotFitOptimizerContract() {
        assertFailsWith<IllegalArgumentException> {
            adapter.optimize(
                settlementId = SettlementId("settlement"),
                debts = listOf(
                    Debt(
                        fromParticipantId = ParticipantId("alice"),
                        toParticipantId = ParticipantId("bob"),
                        amount = Money(Int.MAX_VALUE.toLong() + 1, "USD"),
                    ),
                ),
            )
        }
    }

    private object TestIdGenerator : IdGenerator {
        override fun newSessionId(): SessionId = SessionId("session")
        override fun newParticipantId(): ParticipantId = ParticipantId("participant")
        override fun newExpenseId(): ExpenseId = ExpenseId("expense")
        override fun newSettlementId(): SettlementId = SettlementId("settlement")
        override fun newTransferId(): TransferId = TransferId("transfer")
    }
}
