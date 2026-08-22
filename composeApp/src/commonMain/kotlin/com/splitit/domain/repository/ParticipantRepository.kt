package com.splitit.domain.repository

import com.splitit.domain.model.Participant
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId

interface ParticipantRepository {
    suspend fun getParticipants(groupId: GroupId): List<Participant>
    suspend fun getParticipant(id: ParticipantId): Participant?
    suspend fun saveParticipant(participant: Participant)
    suspend fun deleteParticipant(id: ParticipantId)
    suspend fun isParticipantUsedByExpenses(id: ParticipantId): Boolean
}
