package com.example.splitit.domain.usecase

import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.repository.ParticipantRepository
import com.example.splitit.domain.repository.SessionRepository
import com.example.splitit.domain.value.Clock
import com.example.splitit.domain.value.IdGenerator
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId

class AddParticipantUseCase(
    private val sessionRepository: SessionRepository,
    private val participantRepository: ParticipantRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        sessionId: SessionId,
        name: String,
        avatarColor: String?,
    ): Participant {
        requireNotNull(sessionRepository.getSession(sessionId)) {
            "Session ${sessionId.value} was not found."
        }

        val now = clock.nowMillis()
        val participant = Participant(
            id = idGenerator.newParticipantId(),
            sessionId = sessionId,
            name = name.trim(),
            avatarColor = avatarColor,
            createdAtMillis = now,
            updatedAtMillis = now,
        )

        participantRepository.saveParticipant(participant)
        return participant
    }
}

class UpdateParticipantUseCase(
    private val participantRepository: ParticipantRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        participantId: ParticipantId,
        name: String,
        avatarColor: String?,
    ): Participant {
        val current = requireNotNull(participantRepository.getParticipant(participantId)) {
            "Participant ${participantId.value} was not found."
        }
        val updated = current.copy(
            name = name.trim(),
            avatarColor = avatarColor,
            updatedAtMillis = clock.nowMillis(),
        )

        participantRepository.saveParticipant(updated)
        return updated
    }
}

class RemoveParticipantUseCase(
    private val participantRepository: ParticipantRepository,
) {
    suspend operator fun invoke(participantId: ParticipantId) {
        require(!participantRepository.isParticipantUsedByExpenses(participantId)) {
            "Participant ${participantId.value} cannot be removed because it is used by expenses."
        }

        participantRepository.deleteParticipant(participantId)
    }
}
