package com.splitit.domain.optimizer

import com.splitit.domain.model.Debt
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.Money
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.SettlementId
import com.splitit.domain.value.TransferId
import com.splitit.logic.optimizers.Optimizer
import com.splitit.logic.optimizers.OptimizerResult
import com.splitit.domain.Participant as OptimizerParticipant
import com.splitit.domain.Payment as OptimizerPayment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PaymentOptimizerAdapterHardeningTest {
    @Test
    fun returnsEmptyWithoutCallingOptimizerForNoDebts() {
        val optimizer = RecordingOptimizer()
        val adapter = PaymentOptimizerAdapter(optimizer, TestIdGenerator())

        assertEquals(emptyList(), adapter.optimize(SettlementId("settlement"), emptyList()))
        assertEquals(null, optimizer.received)
    }

    @Test
    fun mapsAndSortsOptimizerTransfersDeterministically() {
        val optimizer = RecordingOptimizer { payments ->
            payments.reversed().toSet()
        }
        val adapter = PaymentOptimizerAdapter(optimizer, TestIdGenerator())
        val debts = listOf(
            Debt(ParticipantId("charlie"), ParticipantId("alice"), Money(30L, "USD")),
            Debt(ParticipantId("bob"), ParticipantId("alice"), Money(20L, "USD")),
        )

        val transfers = adapter.optimize(SettlementId("settlement"), debts)

        assertEquals(listOf("bob", "charlie"), transfers.map { it.fromParticipantId.value })
        assertTrue(optimizer.received.orEmpty().all { it.amount > 0 })
        assertEquals(setOf("alice", "bob", "charlie"), optimizer.received.orEmpty().flatMap {
            listOf(it.from.nickname, it.to.nickname)
        }.toSet())
    }

    @Test
    fun acceptsTheLargestAmountSupportedByOptimizer() {
        val adapter = PaymentOptimizerAdapter(
            optimizer = RecordingOptimizer(),
            idGenerator = TestIdGenerator(),
        )

        val transfers = adapter.optimize(
            settlementId = SettlementId("settlement"),
            debts = listOf(
                Debt(
                    ParticipantId("alice"),
                    ParticipantId("bob"),
                    Money(Int.MAX_VALUE.toLong(), "USD"),
                ),
            ),
        )

        assertEquals(Int.MAX_VALUE.toLong(), transfers.single().amount.minorUnits)
    }

    @Test
    fun rejectsInvalidOptimizerOutputs() {
        val invalidOutputs = listOf<(Set<OptimizerPayment>) -> Set<OptimizerPayment>>(
            { payments ->
                val payment = payments.single()
                setOf(OptimizerPayment(payment.from, payment.to, 0))
            },
            { payments ->
                val payment = payments.single()
                setOf(OptimizerPayment(OptimizerParticipant("unknown"), payment.to, payment.amount))
            },
            { payments ->
                val payment = payments.single()
                setOf(OptimizerPayment(payment.from, payment.to, payment.amount + 1))
            },
        )

        invalidOutputs.forEach { output ->
            assertFailsWith<IllegalArgumentException> {
                PaymentOptimizerAdapter(
                    optimizer = RecordingOptimizer(output),
                    idGenerator = TestIdGenerator(),
                ).optimize(
                    settlementId = SettlementId("settlement"),
                    debts = listOf(
                        Debt(ParticipantId("alice"), ParticipantId("bob"), Money(10L, "USD")),
                    ),
                )
            }
        }
    }

    private class RecordingOptimizer(
        private val output: (Set<OptimizerPayment>) -> Set<OptimizerPayment> = { it },
    ) : Optimizer<OptimizerPayment> {
        var received: Set<OptimizerPayment>? = null

        override fun optimize(elements: Set<OptimizerPayment>): OptimizerResult<OptimizerPayment> {
            received = elements
            return OptimizerResult(optimized = true, elements = output(elements))
        }
    }

    private class TestIdGenerator : IdGenerator {
        override fun newGroupId(): GroupId = GroupId("group")
        override fun newParticipantId(): ParticipantId = ParticipantId("participant")
        override fun newExpenseId(): ExpenseId = ExpenseId("expense")
        override fun newSettlementId(): SettlementId = SettlementId("settlement")
        override fun newTransferId(): TransferId = TransferId("transfer")
    }
}
