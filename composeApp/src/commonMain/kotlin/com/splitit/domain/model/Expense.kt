package com.splitit.domain.model

import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId

enum class ExpenseType {
    EXPENSE,
    TRANSFER_PAYMENT,
}

data class Expense(
    val id: ExpenseId,
    val groupId: GroupId,
    val title: String,
    val amount: Money,
    val payerId: ParticipantId,
    val participantShares: List<ExpenseParticipantShare>,
    val dateMillis: Long,
    val note: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val type: ExpenseType = ExpenseType.EXPENSE,
) {
    init {
        require(title.isNotBlank()) { "Expense title cannot be blank." }
        require(amount.isPositive()) { "Expense amount must be positive." }
        require(participantShares.isNotEmpty()) { "Expense must include at least one participant." }
        require(participantShares.all { it.expenseId == id }) {
            "All participant shares must belong to this expense."
        }
        require(participantShares.map { it.participantId }.toSet().size == participantShares.size) {
            "Expense participant shares cannot contain duplicates."
        }
        require(updatedAtMillis >= createdAtMillis) {
            "Expense updatedAtMillis cannot be earlier than createdAtMillis."
        }
    }

    val isTransferPayment: Boolean
        get() = type == ExpenseType.TRANSFER_PAYMENT
}

data class ExpenseParticipantShare(
    val expenseId: ExpenseId,
    val participantId: ParticipantId,
    val amountMinorUnits: Long,
) {
    init {
        require(amountMinorUnits >= 0) { "Share amount must be non-negative." }
    }
}
