package com.example.splitit.domain.model

import com.example.splitit.domain.value.Money
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import com.example.splitit.domain.value.SettlementId
import com.example.splitit.domain.value.TransferId

data class Balance(
    val participantId: ParticipantId,
    val amount: Money,
)

data class Debt(
    val fromParticipantId: ParticipantId,
    val toParticipantId: ParticipantId,
    val amount: Money,
) {
    init {
        require(fromParticipantId != toParticipantId) { "Debt endpoints must be different." }
        require(amount.isPositive()) { "Debt amount must be positive." }
    }
}

data class Settlement(
    val id: SettlementId,
    val sessionId: SessionId,
    val generatedAtMillis: Long,
    val sourceRevision: Long,
    val transfers: List<SettlementTransfer>,
) {
    init {
        require(sourceRevision >= 0) { "Settlement source revision cannot be negative." }
        require(transfers.all { it.settlementId == id }) {
            "All settlement transfers must belong to this settlement."
        }
    }
}

data class SettlementTransfer(
    val id: TransferId,
    val settlementId: SettlementId,
    val fromParticipantId: ParticipantId,
    val toParticipantId: ParticipantId,
    val amount: Money,
) {
    init {
        require(fromParticipantId != toParticipantId) { "Transfer endpoints must be different." }
        require(amount.isPositive()) { "Transfer amount must be positive." }
    }
}
