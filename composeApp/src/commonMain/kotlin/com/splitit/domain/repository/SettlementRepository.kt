package com.splitit.domain.repository

import com.splitit.domain.model.Settlement
import com.splitit.domain.value.SessionId
import com.splitit.domain.value.SettlementId

interface SettlementRepository {
    suspend fun getLatestSettlement(sessionId: SessionId): Settlement?
    suspend fun getSettlement(id: SettlementId): Settlement?
    suspend fun saveSettlement(settlement: Settlement)
    suspend fun deleteSettlements(sessionId: SessionId)
}
