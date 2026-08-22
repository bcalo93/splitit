package com.splitit.data.repository

import com.splitit.data.database.SplitItDatabase
import com.splitit.data.mapper.toDomain
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId

class SqlDelightGroupRepository(
    private val database: SplitItDatabase,
) : GroupRepository {
    private val queries = database.splitItDatabaseQueries

    override suspend fun getGroups(): List<ExpenseGroup> {
        return queries.selectGroups().executeAsList().map { group ->
            group.toDomain(
                participantIds = queries.selectParticipantsByGroup(group.id)
                    .executeAsList()
                    .map { ParticipantId(it.id) }
                    .toSet(),
                expenseIds = queries.selectExpensesByGroup(group.id)
                    .executeAsList()
                    .map { ExpenseId(it.id) }
                    .toSet(),
            )
        }
    }

    override suspend fun getGroup(id: GroupId): ExpenseGroup? {
        val group = queries.selectGroupById(id.value).executeAsOneOrNull() ?: return null

        return group.toDomain(
            participantIds = queries.selectParticipantsByGroup(group.id)
                .executeAsList()
                .map { ParticipantId(it.id) }
                .toSet(),
            expenseIds = queries.selectExpensesByGroup(group.id)
                .executeAsList()
                .map { ExpenseId(it.id) }
                .toSet(),
        )
    }

    override suspend fun saveGroup(group: ExpenseGroup) {
        database.transaction {
            if (queries.selectGroupById(group.id.value).executeAsOneOrNull() == null) {
                queries.insertGroup(
                    id = group.id.value,
                    title = group.title,
                    description = group.description,
                    created_at = group.createdAtMillis,
                    updated_at = group.updatedAtMillis,
                    status = group.status.name,
                )
            } else {
                queries.updateGroup(
                    title = group.title,
                    description = group.description,
                    updated_at = group.updatedAtMillis,
                    status = group.status.name,
                    id = group.id.value,
                )
            }
        }
    }

    override suspend fun deleteGroup(id: GroupId) {
        database.transaction {
            queries.deleteSettlementTransfersByGroup(id.value)
            queries.deleteSettlementsByGroup(id.value)
            queries.deleteExpenseParticipantsByGroup(id.value)
            queries.deleteExpensesByGroup(id.value)
            queries.deleteParticipantsByGroup(id.value)
            queries.deleteGroup(id.value)
        }
    }
}
