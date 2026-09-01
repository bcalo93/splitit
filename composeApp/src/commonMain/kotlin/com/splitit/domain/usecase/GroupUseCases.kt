package com.splitit.domain.usecase

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.model.Settlement
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.repository.SettlementRepository
import com.splitit.domain.service.SourceRevisionCalculator
import com.splitit.domain.value.Clock
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.IdGenerator

data class CreateGroupParams(
    val title: String,
    val description: String?,
)

class CreateGroupUseCase(
    private val groupRepository: GroupRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) : UseCase<CreateGroupParams, ExpenseGroup> {
    override suspend fun invoke(params: CreateGroupParams): ExpenseGroup {
        val now = clock.nowMillis()
        val group = ExpenseGroup(
            id = idGenerator.newGroupId(),
            title = params.title.trim(),
            description = params.description?.trim()?.takeIf { it.isNotEmpty() },
            createdAtMillis = now,
            updatedAtMillis = now,
        )

        groupRepository.saveGroup(group)
        return group
    }
}

data class UpdateGroupParams(
    val groupId: GroupId,
    val title: String,
    val description: String?,
)

class UpdateGroupUseCase(
    private val groupRepository: GroupRepository,
    private val clock: Clock,
) : UseCase<UpdateGroupParams, ExpenseGroup> {
    override suspend fun invoke(params: UpdateGroupParams): ExpenseGroup {
        val current = requireNotNull(groupRepository.getGroup(params.groupId)) {
            "Group ${params.groupId.value} was not found."
        }
        val updated = current.copy(
            title = params.title.trim(),
            description = params.description?.trim()?.takeIf { it.isNotEmpty() },
            updatedAtMillis = clock.nowMillis(),
        )

        groupRepository.saveGroup(updated)
        return updated
    }
}

data class DeleteGroupParams(
    val groupId: GroupId,
)

class DeleteGroupUseCase(
    private val groupRepository: GroupRepository,
) : UseCase<DeleteGroupParams, Unit> {
    override suspend fun invoke(params: DeleteGroupParams) {
        groupRepository.deleteGroup(params.groupId)
    }
}

object ObserveGroupsParams

class ObserveGroupsUseCase(
    private val groupRepository: GroupRepository,
) : UseCase<ObserveGroupsParams, List<ExpenseGroup>> {
    override suspend fun invoke(params: ObserveGroupsParams): List<ExpenseGroup> {
        return groupRepository.getGroups()
    }
}

data class ObserveGroupDetailsParams(
    val groupId: GroupId,
)

class ObserveGroupDetailsUseCase(
    private val groupRepository: GroupRepository,
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
) : UseCase<ObserveGroupDetailsParams, GroupDetails> {
    override suspend fun invoke(params: ObserveGroupDetailsParams): GroupDetails {
        val group = requireNotNull(groupRepository.getGroup(params.groupId)) {
            "Group ${params.groupId.value} was not found."
        }
        val participants = participantRepository.getParticipants(params.groupId)
        val expenses = expenseRepository.getExpenses(params.groupId)
        val latestSettlement = settlementRepository.getLatestSettlement(params.groupId)

        return GroupDetails(
            group = group,
            participants = participants,
            expenses = expenses,
            latestSettlement = latestSettlement,
        )
    }
}

data class GroupDetails(
    val group: ExpenseGroup,
    val participants: List<Participant>,
    val expenses: List<Expense>,
    val latestSettlement: Settlement?,
) {
    val currentSourceRevision: Long
        get() = SourceRevisionCalculator.calculate(participants, expenses)

    val isSettlementStale: Boolean
        get() = latestSettlement != null && latestSettlement.sourceRevision != currentSourceRevision
}
