package com.example.splitit.domain.repository

import com.example.splitit.domain.model.Settlement
import com.example.splitit.domain.value.SessionId
import com.example.splitit.domain.value.SettlementId

interface SettlementRepository {
    suspend fun getLatestSettlement(sessionId: SessionId): Settlement?
    suspend fun getSettlement(id: SettlementId): Settlement?
    suspend fun saveSettlement(settlement: Settlement)
    suspend fun deleteSettlements(sessionId: SessionId)
}
