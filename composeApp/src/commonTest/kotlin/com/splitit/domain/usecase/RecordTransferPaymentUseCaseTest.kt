package com.splitit.domain.usecase

import com.splitit.domain.model.Debt
import com.splitit.domain.model.ExpenseType
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryGroupRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.TestClock
import com.splitit.testutils.TestIdGenerator
import com.splitit.testutils.TestIds
import com.splitit.testutils.group
import com.splitit.testutils.participant
import com.splitit.testutils.testLocalizationService
import com.splitit.domain.value.Money
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordTransferPaymentUseCaseTest {
    @Test
    fun recordsTransferPaymentAsExpense() = runTest {
        val groupRepository = InMemoryGroupRepository(listOf(group()))
        val participantRepository = InMemoryParticipantRepository(
            listOf(participant(TestIds.alice), participant(TestIds.bob)),
        )
        val expenseRepository = InMemoryExpenseRepository()
        val useCase = RecordTransferPaymentUseCase(
            groupRepository = groupRepository,
            participantRepository = participantRepository,
            expenseRepository = expenseRepository,
            idGenerator = TestIdGenerator(expenseId = TestIds.secondExpense),
            clock = TestClock(30L),
            localization = testLocalizationService,
        )

        val result = useCase(
            groupId = TestIds.group,
            debt = Debt(
                fromParticipantId = TestIds.bob,
                toParticipantId = TestIds.alice,
                amount = Money(500, "USD"),
            ),
        )

        assertEquals(TestIds.secondExpense, result.id)
        assertEquals(ExpenseType.TRANSFER_PAYMENT, result.type)
        assertEquals(TestIds.bob, result.payerId)
        assertEquals(1, result.participantShares.size)
        assertEquals(TestIds.alice, result.participantShares.first().participantId)
        assertEquals(Money(500, "USD"), result.amount)
        assertEquals("Payment: bob → alice", result.title)
        assertTrue(result.isTransferPayment)

        val saved = expenseRepository.getExpense(result.id)
        assertEquals(result, saved)
    }
}
