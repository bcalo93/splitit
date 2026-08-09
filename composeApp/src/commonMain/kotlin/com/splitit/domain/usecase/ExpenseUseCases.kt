package com.splitit.domain.usecase

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseParticipantShare
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.repository.SessionRepository
import com.splitit.domain.value.Clock
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SessionId

class CreateExpenseUseCase(
    private val sessionRepository: SessionRepository,
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        sessionId: SessionId,
        title: String,
        amount: Money,
        payerId: ParticipantId,
        participantIds: List<ParticipantId>,
        dateMillis: Long,
        note: String?,
    ): Expense {
        requireNotNull(sessionRepository.getSession(sessionId)) {
            "Session ${sessionId.value} was not found."
        }
        validateParticipants(sessionId, payerId, participantIds)

        val now = clock.nowMillis()
        val expenseId = idGenerator.newExpenseId()
        val expense = Expense(
            id = expenseId,
            sessionId = sessionId,
            title = title.trim(),
            amount = amount,
            payerId = payerId,
            participantShares = participantIds.distinct().map {
                ExpenseParticipantShare(expenseId = expenseId, participantId = it)
            },
            dateMillis = dateMillis,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            createdAtMillis = now,
            updatedAtMillis = now,
        )

        expenseRepository.saveExpense(expense)
        return expense
    }

    private suspend fun validateParticipants(
        sessionId: SessionId,
        payerId: ParticipantId,
        participantIds: List<ParticipantId>,
    ) {
        require(participantIds.isNotEmpty()) { "Expense must include at least one participant." }
        val sessionParticipantIds = participantRepository.getParticipants(sessionId).map { it.id }.toSet()
        require(payerId in sessionParticipantIds) { "Expense payer must belong to the session." }
        require(participantIds.all { it in sessionParticipantIds }) {
            "Every expense participant must belong to the session."
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
    ): Expense {
        val current = requireNotNull(expenseRepository.getExpense(expenseId)) {
            "Expense ${expenseId.value} was not found."
        }
        val sessionParticipantIds = participantRepository
            .getParticipants(current.sessionId)
            .map { it.id }
            .toSet()

        require(participantIds.isNotEmpty()) { "Expense must include at least one participant." }
        require(payerId in sessionParticipantIds) { "Expense payer must belong to the session." }
        require(participantIds.all { it in sessionParticipantIds }) {
            "Every expense participant must belong to the session."
        }

        val updated = current.copy(
            title = title.trim(),
            amount = amount,
            payerId = payerId,
            participantShares = participantIds.distinct().map {
                ExpenseParticipantShare(expenseId = expenseId, participantId = it)
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
