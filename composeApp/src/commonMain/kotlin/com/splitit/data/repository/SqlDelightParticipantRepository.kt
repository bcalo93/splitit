package com.splitit.data.repository

import com.splitit.data.database.SplitItDatabase
import com.splitit.data.mapper.toDomain
import com.splitit.domain.model.Participant
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SessionId

class SqlDelightParticipantRepository(
    database: SplitItDatabase,
) : ParticipantRepository {
    private val queries = database.splitItDatabaseQueries

    override suspend fun getParticipants(sessionId: SessionId): List<Participant> {
        return queries.selectParticipantsBySession(sessionId.value)
            .executeAsList()
            .map { it.toDomain() }
    }

    override suspend fun getParticipant(id: ParticipantId): Participant? {
        return queries.selectParticipantById(id.value)
            .executeAsOneOrNull()
            ?.toDomain()
    }

    override suspend fun saveParticipant(participant: Participant) {
        queries.upsertParticipant(
            id = participant.id.value,
            session_id = participant.sessionId.value,
            name = participant.name,
            avatar_color = participant.avatarColor,
            created_at = participant.createdAtMillis,
            updated_at = participant.updatedAtMillis,
        )
    }

    override suspend fun deleteParticipant(id: ParticipantId) {
        queries.deleteParticipant(id.value)
    }

    override suspend fun isParticipantUsedByExpenses(id: ParticipantId): Boolean {
        return queries.countExpensesUsingParticipant(
            payer_participant_id = id.value,
            participant_id = id.value,
        ).executeAsList().sum() > 0
    }
}
