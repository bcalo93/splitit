package com.splitit.data.repository

import com.splitit.data.database.SplitItDatabase
import com.splitit.data.mapper.toDomain
import com.splitit.domain.model.ExpenseSession
import com.splitit.domain.repository.SessionRepository
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SessionId

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
