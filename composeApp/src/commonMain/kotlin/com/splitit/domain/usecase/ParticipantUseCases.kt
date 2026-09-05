package com.splitit.domain.usecase

import com.splitit.domain.model.Participant
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.value.Clock
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.ParticipantId

data class AddParticipantParams(
    val groupId: GroupId,
    val name: String,
    val avatarColor: String?,
)

class AddParticipantUseCase(
    private val groupRepository: GroupRepository,
    private val participantRepository: ParticipantRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) : UseCase<AddParticipantParams, Participant> {
    override suspend fun invoke(params: AddParticipantParams): Participant {
        requireNotNull(groupRepository.getGroup(params.groupId)) {
            "Group ${params.groupId.value} was not found."
        }

        val now = clock.nowMillis()
        val participant = Participant(
            id = idGenerator.newParticipantId(),
            groupId = params.groupId,
            name = params.name.trim(),
            avatarColor = params.avatarColor,
            createdAtMillis = now,
            updatedAtMillis = now,
        )

        participantRepository.saveParticipant(participant)
        return participant
    }
}

data class UpdateParticipantParams(
    val participantId: ParticipantId,
    val name: String,
    val avatarColor: String?,
)

class UpdateParticipantUseCase(
    private val participantRepository: ParticipantRepository,
    private val clock: Clock,
) : UseCase<UpdateParticipantParams, Participant> {
    override suspend fun invoke(params: UpdateParticipantParams): Participant {
        val current = requireNotNull(participantRepository.getParticipant(params.participantId)) {
            "Participant ${params.participantId.value} was not found."
        }
        val updated = current.copy(
            name = params.name.trim(),
            avatarColor = params.avatarColor,
            updatedAtMillis = clock.nowMillis(),
        )

        participantRepository.saveParticipant(updated)
        return updated
    }
}

data class RemoveParticipantParams(
    val participantId: ParticipantId,
)

class RemoveParticipantUseCase(
    private val participantRepository: ParticipantRepository,
) : UseCase<RemoveParticipantParams, Unit> {
    override suspend fun invoke(params: RemoveParticipantParams) {
        require(!participantRepository.isParticipantUsedByExpenses(params.participantId)) {
            "Participant ${params.participantId.value} cannot be removed because it is used by expenses."
        }

        participantRepository.deleteParticipant(params.participantId)
    }
}
