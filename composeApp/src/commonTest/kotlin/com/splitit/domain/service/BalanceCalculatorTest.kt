package com.splitit.domain.service

import com.splitit.domain.model.Balance
import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseParticipantShare
import com.splitit.domain.model.Participant
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BalanceCalculatorTest {
    private val calculator = BalanceCalculator()
    private val groupId = GroupId("group")
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
            groupId = groupId,
            title = "Dinner",
            amount = Money(1200, "USD"),
            payerId = aliceId,
            participantShares = listOf(
                ExpenseParticipantShare(expenseId, aliceId, 600L),
                ExpenseParticipantShare(expenseId, charlieId, 600L),
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
            groupId = groupId,
            title = "Dinner",
            amount = Money(1200, "USD"),
            payerId = aliceId,
            participantShares = listOf(
                ExpenseParticipantShare(expenseId, aliceId, 600L),
                ExpenseParticipantShare(expenseId, charlieId, 600L),
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
    fun distributesAmountsExplicitly() {
        val expense = expense(
            amount = 100,
            payerId = aliceId,
            participantIds = listOf(charlieId, bobId, aliceId),
            shareAmounts = mapOf(aliceId to 34L, bobId to 33L, charlieId to 33L),
        )

        val balances = calculator.calculateBalances(participants, listOf(expense))
            .associate { it.participantId to it.amount.minorUnits }

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
            shareAmounts = mapOf(bobId to 67L, charlieId to 33L),
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
        shareAmounts: Map<ParticipantId, Long> = emptyMap(),
        currencyCode: String = "USD",
    ): Expense {
        val amountPerParticipant = if (shareAmounts.isEmpty()) {
            amount / participantIds.size
        } else {
            0L
        }
        return Expense(
            id = id,
            groupId = groupId,
            title = "Expense",
            amount = Money(amount, currencyCode),
            payerId = payerId,
            participantShares = participantIds.map { participantId ->
                ExpenseParticipantShare(
                    id,
                    participantId,
                    shareAmounts[participantId] ?: amountPerParticipant,
                )
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
            groupId = groupId,
            name = id.value,
            avatarColor = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )
    }
}
