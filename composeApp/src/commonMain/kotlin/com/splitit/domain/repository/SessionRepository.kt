package com.splitit.domain.repository

import com.splitit.domain.model.ExpenseSession
import com.splitit.domain.value.SessionId

interface SessionRepository {
    suspend fun getSessions(): List<ExpenseSession>
    suspend fun getSession(id: SessionId): ExpenseSession?
    suspend fun saveSession(session: ExpenseSession)
    suspend fun deleteSession(id: SessionId)
}
