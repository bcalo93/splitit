package com.splitit.domain.model

import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId

data class Participant(
    val id: ParticipantId,
    val groupId: GroupId,
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
