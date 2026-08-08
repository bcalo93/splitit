package com.example.splitit.domain.service

import com.example.splitit.domain.model.Balance
import com.example.splitit.domain.model.Expense
import com.example.splitit.domain.model.ExpenseParticipantShare
import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.Money
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BalanceCalculatorTest {
    private val calculator = BalanceCalculator()
    private val sessionId = SessionId("session")
    private val aliceId = ParticipantId("alice")
    private val bobId = ParticipantId("bob")
    private val charlieId = ParticipantId("charlie")
    private val expenseId = ExpenseId("expense")
    private val participants = listOf(
        participant(aliceId),
        participant(bobId),
        participant(charlieId),
    )

    @Test
    fun calculateBalancesForPartialExpenseParticipants() {
        val expense = Expense(
            id = expenseId,
            sessionId = sessionId,
            title = "Dinner",
            amount = Money(1200, "USD"),
            payerId = aliceId,
            participantShares = listOf(
                ExpenseParticipantShare(expenseId, aliceId),
                ExpenseParticipantShare(expenseId, charlieId),
            ),
            dateMillis = 1,
            note = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )

        val balances = calculator.calculateBalances(participants, listOf(expense))
            .associate { it.participantId to it.amount.minorUnits }

        assertEquals(600, balances.getValue(aliceId))
        assertEquals(0, balances.getValue(bobId))
        assertEquals(-600, balances.getValue(charlieId))
    }

    @Test
    fun calculateDebtsFromBalances() {
        val expense = Expense(
            id = expenseId,
            sessionId = sessionId,
            title = "Dinner",
            amount = Money(1200, "USD"),
            payerId = aliceId,
            participantShares = listOf(
                ExpenseParticipantShare(expenseId, aliceId),
                ExpenseParticipantShare(expenseId, charlieId),
            ),
            dateMillis = 1,
            note = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )

        val debts = calculator.calculateDebts(
            calculator.calculateBalances(participants, listOf(expense)),
        )

        assertEquals(1, debts.size)
        assertEquals(charlieId, debts.first().fromParticipantId)
        assertEquals(aliceId, debts.first().toParticipantId)
        assertEquals(Money(600, "USD"), debts.first().amount)
    }

    @Test
    fun calculateBalancesWhenPayerIsNotIncluded() {
        val expense = expense(
            amount = 1200,
            payerId = aliceId,
            participantIds = listOf(bobId, charlieId),
        )

        val balances = calculator.calculateBalances(participants, listOf(expense))
            .associate { it.participantId to it.amount.minorUnits }

        assertEquals(1200, balances.getValue(aliceId))
        assertEquals(-600, balances.getValue(bobId))
        assertEquals(-600, balances.getValue(charlieId))
        assertEquals(0, balances.values.sum())
    }

    @Test
    fun distributesRemainderByLargestFractionAndStableParticipantId() {
        val expense = expense(
            amount = 100,
            payerId = aliceId,
            participantIds = listOf(charlieId, bobId, aliceId),
        )

        val balances = calculator.calculateBalances(participants, listOf(expense))
            .associate { it.participantId to it.amount.minorUnits }

        // The extra minor unit goes to Alice because equal remainders use ID order.
        assertEquals(66, balances.getValue(aliceId))
        assertEquals(-33, balances.getValue(bobId))
        assertEquals(-33, balances.getValue(charlieId))
        assertEquals(0, balances.values.sum())
    }

    @Test
    fun weightedSplitPreservesAllMinorUnits() {
        val expense = expense(
            amount = 100,
            payerId = aliceId,
            participantIds = listOf(bobId, charlieId),
            weights = mapOf(bobId to 2, charlieId to 1),
        )

        val balances = calculator.calculateBalances(participants, listOf(expense))
            .associate { it.participantId to it.amount.minorUnits }

        assertEquals(100, balances.getValue(aliceId))
        assertEquals(-67, balances.getValue(bobId))
        assertEquals(-33, balances.getValue(charlieId))
        assertEquals(0, balances.values.sum())
    }

    @Test
    fun rejectsExpensesWithDifferentCurrencies() {
        val first = expense(amount = 100, payerId = aliceId, participantIds = listOf(aliceId, bobId))
        val second = expense(
            id = ExpenseId("expense-2"),
            amount = 100,
            payerId = aliceId,
            participantIds = listOf(aliceId, bobId),
            currencyCode = "EUR",
        )

        assertFailsWith<IllegalArgumentException> {
            calculator.calculateBalances(participants, listOf(first, second))
        }
    }

    @Test
    fun rejectsUnbalancedInputWhenCalculatingDebts() {
        assertFailsWith<IllegalArgumentException> {
            calculator.calculateDebts(
                listOf(
                    Balance(aliceId, Money(100, "USD")),
                    Balance(bobId, Money(-50, "USD")),
                ),
            )
        }
    }

    private fun expense(
        id: ExpenseId = expenseId,
        amount: Long,
        payerId: ParticipantId,
        participantIds: List<ParticipantId>,
        weights: Map<ParticipantId, Int> = emptyMap(),
        currencyCode: String = "USD",
    ): Expense {
        return Expense(
            id = id,
            sessionId = sessionId,
            title = "Expense",
            amount = Money(amount, currencyCode),
            payerId = payerId,
            participantShares = participantIds.map { participantId ->
                ExpenseParticipantShare(id, participantId, weights[participantId] ?: 1)
            },
            dateMillis = 1,
            note = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )
    }

    private fun participant(id: ParticipantId): Participant {
        return Participant(
            id = id,
            sessionId = sessionId,
            name = id.value,
            avatarColor = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )
    }
}
