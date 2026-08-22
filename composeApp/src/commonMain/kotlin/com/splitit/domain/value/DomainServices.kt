package com.splitit.domain.value

interface IdGenerator {
    fun newGroupId(): GroupId
    fun newParticipantId(): ParticipantId
    fun newExpenseId(): ExpenseId
    fun newSettlementId(): SettlementId
    fun newTransferId(): TransferId
}

interface Clock {
    fun nowMillis(): Long
}
