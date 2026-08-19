package com.splitit.domain.usecase

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.model.Settlement
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.repository.SettlementRepository
import com.splitit.domain.service.SourceRevisionCalculator
import com.splitit.domain.value.Clock
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.GroupId

class CreateGroupUseCase(
    private val groupRepository: GroupRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
    ): ExpenseGroup {
        val now = clock.nowMillis()
        val group = ExpenseGroup(
            id = idGenerator.newGroupId(),
            title = title.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            createdAtMillis = now,
            updatedAtMillis = now,
        )

        groupRepository.saveGroup(group)
        return group
    }
}

class UpdateGroupUseCase(
    private val groupRepository: GroupRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        groupId: GroupId,
        title: String,
        description: String?,
    ): ExpenseGroup {
        val current = requireNotNull(groupRepository.getGroup(groupId)) {
            "Group ${groupId.value} was not found."
        }
        val updated = current.copy(
            title = title.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            updatedAtMillis = clock.nowMillis(),
        )

        groupRepository.saveGroup(updated)
        return updated
    }
}

class DeleteGroupUseCase(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(groupId: GroupId) {
        groupRepository.deleteGroup(groupId)
    }
}

class ObserveGroupsUseCase(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(): List<ExpenseGroup> {
        return groupRepository.getGroups()
    }
}

class ObserveGroupDetailsUseCase(
    private val groupRepository: GroupRepository,
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
) {
    suspend operator fun invoke(groupId: GroupId): GroupDetails {
        val group = requireNotNull(groupRepository.getGroup(groupId)) {
            "Group ${groupId.value} was not found."
        }
        val participants = participantRepository.getParticipants(groupId)
        val expenses = expenseRepository.getExpenses(groupId)
        val latestSettlement = settlementRepository.getLatestSettlement(groupId)

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
