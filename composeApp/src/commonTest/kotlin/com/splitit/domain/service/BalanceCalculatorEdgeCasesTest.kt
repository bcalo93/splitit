package com.splitit.domain.service

import com.splitit.domain.model.Balance
import com.splitit.domain.model.Debt
import com.splitit.domain.value.Money
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.participant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BalanceCalculatorEdgeCasesTest {
    private val calculator = BalanceCalculator()

    @Test
    fun emptyAndSingleParticipantSessionsHaveZeroBalances() {
        assertEquals(emptyList(), calculator.calculateBalances(emptyList(), emptyList()))

        val balances = calculator.calculateBalances(
            participants = listOf(participant()),
            expenses = emptyList(),
        )

        assertEquals(listOf(Balance(TestIds.alice, Money.zero("USD"))), balances)
    }

    @Test
    fun accumulatesMultipleExpensesAndKeepsParticipantsWithZeroBalance() {
        val participants = listOf(
            participant(TestIds.alice),
            participant(TestIds.bob),
            participant(TestIds.charlie),
        )
        val balances = calculator.calculateBalances(
            participants = participants,
            expenses = listOf(
                expense(
                    amount = Money(900L, "USD"),
                    payerId = TestIds.alice,
                    participantIds = listOf(TestIds.alice, TestIds.bob),
                ),
                expense(
                    id = TestIds.secondExpense,
                    amount = Money(600L, "USD"),
                    payerId = TestIds.bob,
                    participantIds = listOf(TestIds.bob, TestIds.charlie),
                ),
            ),
        ).associate { it.participantId to it.amount.minorUnits }

        assertEquals(450L, balances.getValue(TestIds.alice))
        assertEquals(-150L, balances.getValue(TestIds.bob))
        assertEquals(-300L, balances.getValue(TestIds.charlie))
        assertEquals(0L, balances.values.sum())
    }

    @Test
    fun calculatesDeterministicDebtsForSeveralDebtors() {
        val debts = calculator.calculateDebts(
            listOf(
                Balance(TestIds.alice, Money(100L, "USD")),
                Balance(TestIds.bob, Money(-40L, "USD")),
                Balance(TestIds.charlie, Money(-60L, "USD")),
            ),
        )

        assertEquals(
            listOf(
                Debt(TestIds.bob, TestIds.alice, Money(40L, "USD")),
                Debt(TestIds.charlie, TestIds.alice, Money(60L, "USD")),
            ),
            debts,
        )
    }

    @Test
    fun returnsNoDebtsWhenAllBalancesAreZero() {
        assertEquals(
            emptyList(),
            calculator.calculateDebts(
                listOf(
                    Balance(TestIds.alice, Money.zero("USD")),
                    Balance(TestIds.bob, Money.zero("USD")),
                ),
            ),
        )
    }

    @Test
    fun rejectsExpensesReferencingUnknownParticipants() {
        assertFailsWith<IllegalArgumentException> {
            calculator.calculateBalances(
                participants = listOf(participant(TestIds.alice)),
                expenses = listOf(
                    expense(
                        payerId = TestIds.alice,
                        participantIds = listOf(TestIds.bob),
                    ),
                ),
            )
        }
    }
}
