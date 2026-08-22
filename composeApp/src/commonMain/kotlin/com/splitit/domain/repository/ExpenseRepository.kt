package com.splitit.domain.repository

import com.splitit.domain.model.Expense
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.GroupId

interface ExpenseRepository {
    suspend fun getExpenses(groupId: GroupId): List<Expense>
    suspend fun getExpense(id: ExpenseId): Expense?
    suspend fun saveExpense(expense: Expense)
    suspend fun deleteExpense(id: ExpenseId)
}
