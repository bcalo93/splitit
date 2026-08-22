package com.splitit.domain.service

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseParticipantShare
import com.splitit.domain.model.Participant
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SourceRevisionCalculatorTest {
    private val groupId = GroupId("group")
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
            groupId = groupId,
            name = id.value,
            avatarColor = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )
    }

    private fun expense(): Expense {
        return Expense(
            id = expenseId,
            groupId = groupId,
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
