package com.splitit.domain.usecase

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseParticipantShare
import com.splitit.domain.model.ExpenseType
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.value.Clock
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId

class CreateExpenseUseCase(
    private val groupRepository: GroupRepository,
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        groupId: GroupId,
        title: String,
        amount: Money,
        payerId: ParticipantId,
        participantIds: List<ParticipantId>,
        dateMillis: Long,
        note: String?,
        shareWeights: Map<ParticipantId, Int> = emptyMap(),
        type: ExpenseType = ExpenseType.EXPENSE,
    ): Expense {
        requireNotNull(groupRepository.getGroup(groupId)) {
            "Group ${groupId.value} was not found."
        }
        validateParticipants(groupId, payerId, participantIds)

        val now = clock.nowMillis()
        val expenseId = idGenerator.newExpenseId()
        val expense = Expense(
            id = expenseId,
            groupId = groupId,
            title = title.trim(),
            amount = amount,
            payerId = payerId,
            participantShares = participantIds.distinct().map {
                ExpenseParticipantShare(
                    expenseId = expenseId,
                    participantId = it,
                    shareWeight = shareWeights[it]?.coerceAtLeast(1) ?: 1,
                )
            },
            dateMillis = dateMillis,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            createdAtMillis = now,
            updatedAtMillis = now,
            type = type,
        )

        expenseRepository.saveExpense(expense)
        return expense
    }

    private suspend fun validateParticipants(
        groupId: GroupId,
        payerId: ParticipantId,
        participantIds: List<ParticipantId>,
    ) {
        require(participantIds.isNotEmpty()) { "Expense must include at least one participant." }
        val groupParticipantIds = participantRepository.getParticipants(groupId).map { it.id }.toSet()
        require(payerId in groupParticipantIds) { "Expense payer must belong to the group." }
        require(participantIds.all { it in groupParticipantIds }) {
            "Every expense participant must belong to the group."
        }
    }
}

class UpdateExpenseUseCase(
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        expenseId: ExpenseId,
        title: String,
        amount: Money,
        payerId: ParticipantId,
        participantIds: List<ParticipantId>,
        dateMillis: Long,
        note: String?,
        shareWeights: Map<ParticipantId, Int> = emptyMap(),
    ): Expense {
        val current = requireNotNull(expenseRepository.getExpense(expenseId)) {
            "Expense ${expenseId.value} was not found."
        }
        val groupParticipantIds = participantRepository
            .getParticipants(current.groupId)
            .map { it.id }
            .toSet()

        require(participantIds.isNotEmpty()) { "Expense must include at least one participant." }
        require(payerId in groupParticipantIds) { "Expense payer must belong to the group." }
        require(participantIds.all { it in groupParticipantIds }) {
            "Every expense participant must belong to the group."
        }

        val updated = current.copy(
            title = title.trim(),
            amount = amount,
            payerId = payerId,
            participantShares = participantIds.distinct().map {
                ExpenseParticipantShare(
                    expenseId = expenseId,
                    participantId = it,
                    shareWeight = shareWeights[it]?.coerceAtLeast(1) ?: 1,
                )
            },
            dateMillis = dateMillis,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            updatedAtMillis = clock.nowMillis(),
        )

        expenseRepository.saveExpense(updated)
        return updated
    }
}

class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(expenseId: ExpenseId) {
        expenseRepository.deleteExpense(expenseId)
    }
}
