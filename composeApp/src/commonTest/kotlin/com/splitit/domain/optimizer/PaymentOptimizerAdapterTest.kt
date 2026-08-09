package com.splitit.domain.optimizer

import com.splitit.domain.model.Debt
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SessionId
import com.splitit.domain.value.SettlementId
import com.splitit.domain.value.TransferId
import com.splitit.logic.optimizers.ComposedOptimizer
import com.splitit.logic.optimizers.debt.CycleOptimizer
import com.splitit.logic.optimizers.debt.TransitiveOptimizer
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

    @Test
    fun rejectsDebtsWithDifferentCurrencies() {
        assertFailsWith<IllegalArgumentException> {
            adapter.optimize(
                settlementId = SettlementId("settlement"),
                debts = listOf(
                    Debt(ParticipantId("alice"), ParticipantId("bob"), Money(100, "USD")),
                    Debt(ParticipantId("bob"), ParticipantId("alice"), Money(40, "EUR")),
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
