package com.splitit.domain.model

import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.participant
import com.splitit.testutils.settlement
import com.splitit.testutils.share
import com.splitit.testutils.transfer
import com.splitit.testutils.group
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId
import kotlin.test.Test
import kotlin.test.assertFailsWith

class DomainModelValidationTest {
    @Test
    fun rejectsBlankIds() {
        assertFailsWith<IllegalArgumentException> { GroupId(" ") }
        assertFailsWith<IllegalArgumentException> { ParticipantId("") }
        assertFailsWith<IllegalArgumentException> { ExpenseId("\t") }
    }

    @Test
    fun rejectsInvalidGroupAndParticipantValues() {
        assertFailsWith<IllegalArgumentException> { group(title = " ") }
        assertFailsWith<IllegalArgumentException> {
            group(createdAtMillis = 2L, updatedAtMillis = 1L)
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
