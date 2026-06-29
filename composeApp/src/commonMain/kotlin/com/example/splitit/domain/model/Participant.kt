package com.example.splitit.domain.model

import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId

data class Participant(
    val id: ParticipantId,
    val sessionId: SessionId,
    val name: String,
    val avatarColor: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    init {
        require(name.isNotBlank()) { "Participant name cannot be blank." }
        require(updatedAtMillis >= createdAtMillis) {
            "Participant updatedAtMillis cannot be earlier than createdAtMillis."
        }
    }
}
