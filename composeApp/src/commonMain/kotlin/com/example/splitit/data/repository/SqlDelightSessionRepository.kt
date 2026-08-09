package com.example.splitit.data.repository

import com.example.splitit.data.database.SplitItDatabase
import com.example.splitit.data.mapper.toDomain
import com.example.splitit.domain.model.ExpenseSession
import com.example.splitit.domain.repository.SessionRepository
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId

class SqlDelightSessionRepository(
    private val database: SplitItDatabase,
) : SessionRepository {
    private val queries = database.splitItDatabaseQueries

    override suspend fun getSessions(): List<ExpenseSession> {
        return queries.selectSessions().executeAsList().map { session ->
            session.toDomain(
                participantIds = queries.selectParticipantsBySession(session.id)
                    .executeAsList()
                    .map { ParticipantId(it.id) }
                    .toSet(),
                expenseIds = queries.selectExpensesBySession(session.id)
                    .executeAsList()
                    .map { ExpenseId(it.id) }
                    .toSet(),
            )
        }
    }

    override suspend fun getSession(id: SessionId): ExpenseSession? {
        val session = queries.selectSessionById(id.value).executeAsOneOrNull() ?: return null

        return session.toDomain(
            participantIds = queries.selectParticipantsBySession(session.id)
                .executeAsList()
                .map { ParticipantId(it.id) }
                .toSet(),
            expenseIds = queries.selectExpensesBySession(session.id)
                .executeAsList()
                .map { ExpenseId(it.id) }
                .toSet(),
        )
    }

    override suspend fun saveSession(session: ExpenseSession) {
        database.transaction {
            if (queries.selectSessionById(session.id.value).executeAsOneOrNull() == null) {
                queries.insertSession(
                    id = session.id.value,
                    title = session.title,
                    description = session.description,
                    created_at = session.createdAtMillis,
                    updated_at = session.updatedAtMillis,
                    status = session.status.name,
                )
            } else {
                queries.updateSession(
                    title = session.title,
                    description = session.description,
                    updated_at = session.updatedAtMillis,
                    status = session.status.name,
                    id = session.id.value,
                )
            }
        }
    }

    override suspend fun deleteSession(id: SessionId) {
        database.transaction {
            queries.deleteSettlementTransfersBySession(id.value)
            queries.deleteSettlementsBySession(id.value)
            queries.deleteExpenseParticipantsBySession(id.value)
            queries.deleteExpensesBySession(id.value)
            queries.deleteParticipantsBySession(id.value)
            queries.deleteSession(id.value)
        }
    }
}
