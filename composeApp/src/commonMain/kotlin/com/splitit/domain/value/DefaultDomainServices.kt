package com.splitit.domain.value

import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UuidGenerator : IdGenerator {
    override fun newSessionId(): SessionId = SessionId(Uuid.random().toString())

    override fun newParticipantId(): ParticipantId = ParticipantId(Uuid.random().toString())

    override fun newExpenseId(): ExpenseId = ExpenseId(Uuid.random().toString())

    override fun newSettlementId(): SettlementId = SettlementId(Uuid.random().toString())

    override fun newTransferId(): TransferId = TransferId(Uuid.random().toString())
}

@OptIn(ExperimentalTime::class)
class SystemClock : Clock {
    override fun nowMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
}
