package com.example.splitit.domain.service

import com.example.splitit.domain.model.Expense
import com.example.splitit.domain.model.ExpenseParticipantShare
import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.Money
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import kotlin.test.Test
import kotlin.test.assertEquals

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
