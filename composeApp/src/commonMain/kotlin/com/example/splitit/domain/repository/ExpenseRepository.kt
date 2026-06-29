package com.example.splitit.domain.repository

import com.example.splitit.domain.model.Expense
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.SessionId

interface ExpenseRepository {
    suspend fun getExpenses(sessionId: SessionId): List<Expense>
    suspend fun getExpense(id: ExpenseId): Expense?
    suspend fun saveExpense(expense: Expense)
    suspend fun deleteExpense(id: ExpenseId)
}
