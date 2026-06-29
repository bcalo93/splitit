package com.example.splitit.domain.repository

import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId

interface ParticipantRepository {
    suspend fun getParticipants(sessionId: SessionId): List<Participant>
    suspend fun getParticipant(id: ParticipantId): Participant?
    suspend fun saveParticipant(participant: Participant)
    suspend fun deleteParticipant(id: ParticipantId)
    suspend fun isParticipantUsedByExpenses(id: ParticipantId): Boolean
}
