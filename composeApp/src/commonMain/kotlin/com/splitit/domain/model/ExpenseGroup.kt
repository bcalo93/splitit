package com.splitit.domain.model

import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId

data class ExpenseGroup(
    val id: GroupId,
    val title: String,
    val description: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val participantIds: Set<ParticipantId> = emptySet(),
    val expenseIds: Set<ExpenseId> = emptySet(),
    val status: GroupStatus = GroupStatus.Active,
) {
    init {
        require(title.isNotBlank()) { "Group title cannot be blank." }
        require(updatedAtMillis >= createdAtMillis) {
            "Group updatedAtMillis cannot be earlier than createdAtMillis."
        }
    }
}

enum class GroupStatus {
    Active,
    Archived,
}
