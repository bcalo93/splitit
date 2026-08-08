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
import kotlin.test.assertNotEquals

class SourceRevisionCalculatorTest {
    private val sessionId = SessionId("session")
    private val aliceId = ParticipantId("alice")
    private val bobId = ParticipantId("bob")
    private val expenseId = ExpenseId("expense")

    @Test
    fun sourceRevisionDoesNotDependOnInputOrder() {
        val alice = participant(aliceId)
        val bob = participant(bobId)
        val expense = expense()

        assertEquals(
            SourceRevisionCalculator.calculate(listOf(alice, bob), listOf(expense)),
            SourceRevisionCalculator.calculate(listOf(bob, alice), listOf(expense)),
        )
    }

    @Test
    fun sourceRevisionChangesWhenAnExpenseIsRemovedAtTheSameTimestamp() {
        val participants = listOf(participant(aliceId), participant(bobId))
        val expense = expense()
        val original = SourceRevisionCalculator.calculate(participants, listOf(expense))
        val afterRemoval = SourceRevisionCalculator.calculate(participants, emptyList())

        assertNotEquals(original, afterRemoval)
    }

    @Test
    fun sourceRevisionChangesWhenParticipantDataChanges() {
        val original = participant(aliceId)
        val renamed = original.copy(name = "Alice renamed", updatedAtMillis = original.updatedAtMillis)

        assertNotEquals(
            SourceRevisionCalculator.calculate(listOf(original), emptyList()),
            SourceRevisionCalculator.calculate(listOf(renamed), emptyList()),
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

    private fun expense(): Expense {
        return Expense(
            id = expenseId,
            sessionId = sessionId,
            title = "Dinner",
            amount = Money(1000, "USD"),
            payerId = aliceId,
            participantShares = listOf(
                ExpenseParticipantShare(expenseId, aliceId),
                ExpenseParticipantShare(expenseId, bobId),
            ),
            dateMillis = 1,
            note = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )
    }
}
