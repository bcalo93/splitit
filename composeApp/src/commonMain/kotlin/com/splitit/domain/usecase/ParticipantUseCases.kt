package com.splitit.domain.usecase

import com.splitit.domain.model.Participant
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.value.Clock
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId

class AddParticipantUseCase(
    private val groupRepository: GroupRepository,
    private val participantRepository: ParticipantRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        groupId: GroupId,
        name: String,
        avatarColor: String?,
    ): Participant {
        requireNotNull(groupRepository.getGroup(groupId)) {
            "Group ${groupId.value} was not found."
        }

        val now = clock.nowMillis()
        val participant = Participant(
            id = idGenerator.newParticipantId(),
            groupId = groupId,
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
