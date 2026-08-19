package com.splitit.domain.repository

import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.value.GroupId

interface GroupRepository {
    suspend fun getGroups(): List<ExpenseGroup>
    suspend fun getGroup(id: GroupId): ExpenseGroup?
    suspend fun saveGroup(group: ExpenseGroup)
    suspend fun deleteGroup(id: GroupId)
}
