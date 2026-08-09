package com.example.splitit.domain.model

import com.example.splitit.testutils.TestIds
import com.example.splitit.testutils.expense
import com.example.splitit.testutils.participant
import com.example.splitit.testutils.settlement
import com.example.splitit.testutils.share
import com.example.splitit.testutils.transfer
import com.example.splitit.testutils.session
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.Money
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import kotlin.test.Test
import kotlin.test.assertFailsWith

class DomainModelValidationTest {
    @Test
    fun rejectsBlankIds() {
        assertFailsWith<IllegalArgumentException> { SessionId(" ") }
        assertFailsWith<IllegalArgumentException> { ParticipantId("") }
        assertFailsWith<IllegalArgumentException> { ExpenseId("\t") }
    }

    @Test
    fun rejectsInvalidSessionAndParticipantValues() {
        assertFailsWith<IllegalArgumentException> { session(title = " ") }
        assertFailsWith<IllegalArgumentException> {
            session(createdAtMillis = 2L, updatedAtMillis = 1L)
        }
        assertFailsWith<IllegalArgumentException> { participant(name = "") }
        assertFailsWith<IllegalArgumentException> {
            participant(createdAtMillis = 2L, updatedAtMillis = 1L)
        }
    }

    @Test
    fun rejectsInvalidExpenseSharesAndAmounts() {
        assertFailsWith<IllegalArgumentException> {
            expense(amount = Money.zero("USD"))
        }
        assertFailsWith<IllegalArgumentException> {
            expense(participantIds = emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            expense(participantIds = listOf(TestIds.alice, TestIds.alice))
        }
        assertFailsWith<IllegalArgumentException> {
            expense(
                participantIds = listOf(TestIds.alice),
                weights = mapOf(TestIds.alice to 0),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            expense(
                participantIds = listOf(TestIds.alice),
                id = TestIds.expense,
            ).copy(
                participantShares = listOf(share(expenseId = TestIds.secondExpense)),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            expense(createdAtMillis = 2L, updatedAtMillis = 1L)
        }
    }

    @Test
    fun rejectsInvalidDebtSettlementAndTransferValues() {
        assertFailsWith<IllegalArgumentException> {
            Debt(TestIds.alice, TestIds.alice, Money(1L, "USD"))
        }
        assertFailsWith<IllegalArgumentException> {
            Debt(TestIds.alice, TestIds.bob, Money(0L, "USD"))
        }
        assertFailsWith<IllegalArgumentException> {
            transfer(fromParticipantId = TestIds.alice, toParticipantId = TestIds.alice)
        }
        assertFailsWith<IllegalArgumentException> {
            settlement(sourceRevision = -1L)
        }
        assertFailsWith<IllegalArgumentException> {
            settlement(transfers = listOf(transfer(settlementId = TestIds.secondSettlement)))
        }
    }
}
