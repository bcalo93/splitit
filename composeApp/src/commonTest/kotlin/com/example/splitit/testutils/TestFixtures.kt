package com.example.splitit.testutils

import com.example.splitit.domain.model.Expense
import com.example.splitit.domain.model.ExpenseParticipantShare
import com.example.splitit.domain.model.ExpenseSession
import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.model.SessionStatus
import com.example.splitit.domain.model.Settlement
import com.example.splitit.domain.model.SettlementTransfer
import com.example.splitit.domain.value.Clock
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.IdGenerator
import com.example.splitit.domain.value.Money
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import com.example.splitit.domain.value.SettlementId
import com.example.splitit.domain.value.TransferId

object TestIds {
    val session = SessionId("session")
    val secondSession = SessionId("session-2")
    val alice = ParticipantId("alice")
    val bob = ParticipantId("bob")
    val charlie = ParticipantId("charlie")
    val expense = ExpenseId("expense")
    val secondExpense = ExpenseId("expense-2")
    val settlement = SettlementId("settlement")
    val secondSettlement = SettlementId("settlement-2")
    val transfer = TransferId("transfer")
}

class TestClock(var currentTimeMillis: Long = 10L) : Clock {
    override fun nowMillis(): Long = currentTimeMillis
}

class TestIdGenerator(
    private val sessionId: SessionId = TestIds.session,
    private val participantId: ParticipantId = TestIds.alice,
    private val expenseId: ExpenseId = TestIds.expense,
    private val settlementId: SettlementId = TestIds.settlement,
    private val transferId: TransferId = TestIds.transfer,
) : IdGenerator {
    override fun newSessionId(): SessionId = sessionId

    override fun newParticipantId(): ParticipantId = participantId

    override fun newExpenseId(): ExpenseId = expenseId

    override fun newSettlementId(): SettlementId = settlementId

    override fun newTransferId(): TransferId = transferId
}

fun session(
    id: SessionId = TestIds.session,
    title: String = "Trip",
    description: String? = null,
    createdAtMillis: Long = 1L,
    updatedAtMillis: Long = createdAtMillis,
    participantIds: Set<ParticipantId> = emptySet(),
    expenseIds: Set<ExpenseId> = emptySet(),
    status: SessionStatus = SessionStatus.Active,
): ExpenseSession {
    return ExpenseSession(
        id = id,
        title = title,
        description = description,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        participantIds = participantIds,
        expenseIds = expenseIds,
        status = status,
    )
}

fun participant(
    id: ParticipantId = TestIds.alice,
    sessionId: SessionId = TestIds.session,
    name: String = id.value,
    avatarColor: String? = null,
    createdAtMillis: Long = 1L,
    updatedAtMillis: Long = createdAtMillis,
): Participant {
    return Participant(
        id = id,
        sessionId = sessionId,
        name = name,
        avatarColor = avatarColor,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

fun expense(
    id: ExpenseId = TestIds.expense,
    sessionId: SessionId = TestIds.session,
    title: String = "Dinner",
    amount: Money = Money(1_000L, "USD"),
    payerId: ParticipantId = TestIds.alice,
    participantIds: List<ParticipantId> = listOf(TestIds.alice, TestIds.bob),
    weights: Map<ParticipantId, Int> = emptyMap(),
    dateMillis: Long = 1L,
    note: String? = null,
    createdAtMillis: Long = 1L,
    updatedAtMillis: Long = createdAtMillis,
): Expense {
    return Expense(
        id = id,
        sessionId = sessionId,
        title = title,
        amount = amount,
        payerId = payerId,
        participantShares = participantIds.map { participantId ->
            ExpenseParticipantShare(
                expenseId = id,
                participantId = participantId,
                shareWeight = weights[participantId] ?: 1,
            )
        },
        dateMillis = dateMillis,
        note = note,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

fun share(
    expenseId: ExpenseId = TestIds.expense,
    participantId: ParticipantId = TestIds.alice,
    shareWeight: Int = 1,
): ExpenseParticipantShare {
    return ExpenseParticipantShare(expenseId, participantId, shareWeight)
}

fun settlement(
    id: SettlementId = TestIds.settlement,
    sessionId: SessionId = TestIds.session,
    generatedAtMillis: Long = 10L,
    sourceRevision: Long = 1L,
    transfers: List<SettlementTransfer> = emptyList(),
): Settlement {
    return Settlement(
        id = id,
        sessionId = sessionId,
        generatedAtMillis = generatedAtMillis,
        sourceRevision = sourceRevision,
        transfers = transfers,
    )
}

fun transfer(
    id: TransferId = TestIds.transfer,
    settlementId: SettlementId = TestIds.settlement,
    fromParticipantId: ParticipantId = TestIds.bob,
    toParticipantId: ParticipantId = TestIds.alice,
    amount: Money = Money(500L, "USD"),
): SettlementTransfer {
    return SettlementTransfer(
        id = id,
        settlementId = settlementId,
        fromParticipantId = fromParticipantId,
        toParticipantId = toParticipantId,
        amount = amount,
    )
}
