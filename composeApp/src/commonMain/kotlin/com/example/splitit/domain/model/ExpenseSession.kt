package com.example.splitit.domain.model

import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId

data class ExpenseSession(
    val id: SessionId,
    val title: String,
    val description: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val participantIds: Set<ParticipantId> = emptySet(),
    val expenseIds: Set<ExpenseId> = emptySet(),
    val status: SessionStatus = SessionStatus.Active,
) {
    init {
        require(title.isNotBlank()) { "Session title cannot be blank." }
        require(updatedAtMillis >= createdAtMillis) {
            "Session updatedAtMillis cannot be earlier than createdAtMillis."
        }
    }
}

enum class SessionStatus {
    Active,
    Archived,
}
