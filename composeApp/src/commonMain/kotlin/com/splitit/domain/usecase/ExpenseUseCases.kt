package com.splitit.domain.usecase

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseParticipantShare
import com.splitit.domain.model.ExpenseType
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.value.Clock
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId

data class CreateExpenseParams(
    val groupId: GroupId,
    val title: String,
    val amount: Money,
    val payerId: ParticipantId,
    val participantIds: List<ParticipantId>,
    val dateMillis: Long,
    val note: String?,
    val shareAmounts: Map<ParticipantId, Long> = emptyMap(),
    val type: ExpenseType = ExpenseType.EXPENSE,
)

class CreateExpenseUseCase(
    private val groupRepository: GroupRepository,
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) : UseCase<CreateExpenseParams, Expense> {
    override suspend fun invoke(params: CreateExpenseParams): Expense {
        requireNotNull(groupRepository.getGroup(params.groupId)) {
            "Group ${params.groupId.value} was not found."
        }
        validateParticipants(params.groupId, params.payerId, params.participantIds)

        val now = clock.nowMillis()
        val expenseId = idGenerator.newExpenseId()
        val expense = Expense(
            id = expenseId,
            groupId = params.groupId,
            title = params.title.trim(),
            amount = params.amount,
            payerId = params.payerId,
            participantShares = params.participantIds.distinct().map {
                ExpenseParticipantShare(
                    expenseId = expenseId,
                    participantId = it,
                    amountMinorUnits = params.shareAmounts[it] ?: 0L,
                )
            },
            dateMillis = params.dateMillis,
            note = params.note?.trim()?.takeIf { it.isNotEmpty() },
            createdAtMillis = now,
            updatedAtMillis = now,
            type = params.type,
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

data class UpdateExpenseParams(
    val expenseId: ExpenseId,
    val title: String,
    val amount: Money,
    val payerId: ParticipantId,
    val participantIds: List<ParticipantId>,
    val dateMillis: Long,
    val note: String?,
    val shareAmounts: Map<ParticipantId, Long> = emptyMap(),
)

class UpdateExpenseUseCase(
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val clock: Clock,
) : UseCase<UpdateExpenseParams, Expense> {
    override suspend fun invoke(params: UpdateExpenseParams): Expense {
        val current = requireNotNull(expenseRepository.getExpense(params.expenseId)) {
            "Expense ${params.expenseId.value} was not found."
        }
        val groupParticipantIds = participantRepository
            .getParticipants(current.groupId)
            .map { it.id }
            .toSet()

        require(params.participantIds.isNotEmpty()) { "Expense must include at least one participant." }
        require(params.payerId in groupParticipantIds) { "Expense payer must belong to the group." }
        require(params.participantIds.all { it in groupParticipantIds }) {
            "Every expense participant must belong to the group."
        }

        val updated = current.copy(
            title = params.title.trim(),
            amount = params.amount,
            payerId = params.payerId,
            participantShares = params.participantIds.distinct().map {
                ExpenseParticipantShare(
                    expenseId = params.expenseId,
                    participantId = it,
                    amountMinorUnits = params.shareAmounts[it] ?: 0L,
                )
            },
            dateMillis = params.dateMillis,
            note = params.note?.trim()?.takeIf { it.isNotEmpty() },
            updatedAtMillis = clock.nowMillis(),
        )

        expenseRepository.saveExpense(updated)
        return updated
    }
}

data class DeleteExpenseParams(
    val expenseId: ExpenseId,
)

class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
) : UseCase<DeleteExpenseParams, Unit> {
    override suspend fun invoke(params: DeleteExpenseParams) {
        expenseRepository.deleteExpense(params.expenseId)
    }
}
