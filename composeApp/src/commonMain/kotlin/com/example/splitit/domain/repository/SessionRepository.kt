package com.example.splitit.domain.repository

import com.example.splitit.domain.model.ExpenseSession
import com.example.splitit.domain.value.SessionId

interface SessionRepository {
    suspend fun getSessions(): List<ExpenseSession>
    suspend fun getSession(id: SessionId): ExpenseSession?
    suspend fun saveSession(session: ExpenseSession)
    suspend fun deleteSession(id: SessionId)
}
