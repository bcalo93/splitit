package com.splitit.domain.model

import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.SettlementId
import com.splitit.domain.value.TransferId

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
    val groupId: GroupId,
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
