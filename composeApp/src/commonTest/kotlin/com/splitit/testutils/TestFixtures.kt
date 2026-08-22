package com.splitit.testutils

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseParticipantShare
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.model.GroupStatus
import com.splitit.domain.model.Settlement
import com.splitit.domain.model.SettlementTransfer
import com.splitit.domain.value.Clock
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.SettlementId
import com.splitit.domain.value.TransferId

object TestIds {
    val group = GroupId("group")
    val secondGroup = GroupId("group-2")
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
    private val groupId: GroupId = TestIds.group,
    private val participantId: ParticipantId = TestIds.alice,
    private val expenseId: ExpenseId = TestIds.expense,
    private val settlementId: SettlementId = TestIds.settlement,
    private val transferId: TransferId = TestIds.transfer,
) : IdGenerator {
    override fun newGroupId(): GroupId = groupId

    override fun newParticipantId(): ParticipantId = participantId

    override fun newExpenseId(): ExpenseId = expenseId

    override fun newSettlementId(): SettlementId = settlementId

    override fun newTransferId(): TransferId = transferId
}

fun group(
    id: GroupId = TestIds.group,
    title: String = "Trip",
    description: String? = null,
    createdAtMillis: Long = 1L,
    updatedAtMillis: Long = createdAtMillis,
    participantIds: Set<ParticipantId> = emptySet(),
    expenseIds: Set<ExpenseId> = emptySet(),
    status: GroupStatus = GroupStatus.Active,
): ExpenseGroup {
    return ExpenseGroup(
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
    groupId: GroupId = TestIds.group,
    name: String = id.value,
    avatarColor: String? = null,
    createdAtMillis: Long = 1L,
    updatedAtMillis: Long = createdAtMillis,
): Participant {
    return Participant(
        id = id,
        groupId = groupId,
        name = name,
        avatarColor = avatarColor,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

fun expense(
    id: ExpenseId = TestIds.expense,
    groupId: GroupId = TestIds.group,
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
        groupId = groupId,
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
    groupId: GroupId = TestIds.group,
    generatedAtMillis: Long = 10L,
    sourceRevision: Long = 1L,
    transfers: List<SettlementTransfer> = emptyList(),
): Settlement {
    return Settlement(
        id = id,
        groupId = groupId,
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
