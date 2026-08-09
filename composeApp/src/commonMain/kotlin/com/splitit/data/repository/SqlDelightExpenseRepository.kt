package com.splitit.data.repository

import com.splitit.data.database.SplitItDatabase
import com.splitit.data.mapper.toDomain
import com.splitit.domain.model.Expense
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.SessionId

class SqlDelightExpenseRepository(
    private val database: SplitItDatabase,
) : ExpenseRepository {
    private val queries = database.splitItDatabaseQueries

    override suspend fun getExpenses(sessionId: SessionId): List<Expense> {
        return queries.selectExpensesBySession(sessionId.value)
            .executeAsList()
            .map { expense ->
                expense.toDomain(
                    shares = queries.selectExpenseParticipants(expense.id)
                        .executeAsList()
                        .map { it.toDomain() },
                )
            }
    }

    override suspend fun getExpense(id: ExpenseId): Expense? {
        val expense = queries.selectExpenseById(id.value).executeAsOneOrNull() ?: return null
        return expense.toDomain(
            shares = queries.selectExpenseParticipants(expense.id)
                .executeAsList()
                .map { it.toDomain() },
        )
    }

    override suspend fun saveExpense(expense: Expense) {
        database.transaction {
            queries.upsertExpense(
                id = expense.id.value,
                session_id = expense.sessionId.value,
                title = expense.title,
                amount_minor = expense.amount.minorUnits,
                currency_code = expense.amount.currencyCode,
                payer_participant_id = expense.payerId.value,
                date_millis = expense.dateMillis,
                note = expense.note,
                created_at = expense.createdAtMillis,
                updated_at = expense.updatedAtMillis,
            )
            queries.deleteExpenseParticipants(expense.id.value)
            expense.participantShares.forEach { share ->
                queries.insertExpenseParticipant(
                    expense_id = share.expenseId.value,
                    participant_id = share.participantId.value,
                    share_weight = share.shareWeight.toLong(),
                )
            }
        }
    }

    override suspend fun deleteExpense(id: ExpenseId) {
        database.transaction {
            queries.deleteExpenseParticipants(id.value)
            queries.deleteExpense(id.value)
        }
    }
}
