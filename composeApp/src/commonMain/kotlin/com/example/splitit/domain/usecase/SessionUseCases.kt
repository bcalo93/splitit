package com.example.splitit.domain.usecase

import com.example.splitit.domain.model.ExpenseSession
import com.example.splitit.domain.repository.SessionRepository
import com.example.splitit.domain.service.SourceRevisionCalculator
import com.example.splitit.domain.value.Clock
import com.example.splitit.domain.value.IdGenerator
import com.example.splitit.domain.value.SessionId

class CreateSessionUseCase(
    private val sessionRepository: SessionRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
    ): ExpenseSession {
        val now = clock.nowMillis()
        val session = ExpenseSession(
            id = idGenerator.newSessionId(),
            title = title.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            createdAtMillis = now,
            updatedAtMillis = now,
        )

        sessionRepository.saveSession(session)
        return session
    }
}

class UpdateSessionUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        sessionId: SessionId,
        title: String,
        description: String?,
    ): ExpenseSession {
        val current = requireNotNull(sessionRepository.getSession(sessionId)) {
            "Session ${sessionId.value} was not found."
        }
        val updated = current.copy(
            title = title.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            updatedAtMillis = clock.nowMillis(),
        )

        sessionRepository.saveSession(updated)
        return updated
    }
}

class DeleteSessionUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(sessionId: SessionId) {
        sessionRepository.deleteSession(sessionId)
    }
}

class ObserveSessionsUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(): List<ExpenseSession> {
        return sessionRepository.getSessions()
    }
}

class ObserveSessionDetailsUseCase(
    private val sessionRepository: SessionRepository,
    private val participantRepository: com.example.splitit.domain.repository.ParticipantRepository,
    private val expenseRepository: com.example.splitit.domain.repository.ExpenseRepository,
    private val settlementRepository: com.example.splitit.domain.repository.SettlementRepository,
) {
    suspend operator fun invoke(sessionId: SessionId): SessionDetails {
        val session = requireNotNull(sessionRepository.getSession(sessionId)) {
            "Session ${sessionId.value} was not found."
        }
        val participants = participantRepository.getParticipants(sessionId)
        val expenses = expenseRepository.getExpenses(sessionId)
        val latestSettlement = settlementRepository.getLatestSettlement(sessionId)

        return SessionDetails(
            session = session,
            participants = participants,
            expenses = expenses,
            latestSettlement = latestSettlement,
        )
    }
}

data class SessionDetails(
    val session: ExpenseSession,
    val participants: List<com.example.splitit.domain.model.Participant>,
    val expenses: List<com.example.splitit.domain.model.Expense>,
    val latestSettlement: com.example.splitit.domain.model.Settlement?,
) {
    val currentSourceRevision: Long
        get() = SourceRevisionCalculator.calculate(participants, expenses)

    val isSettlementStale: Boolean
        get() = latestSettlement != null && latestSettlement.sourceRevision != currentSourceRevision
}
