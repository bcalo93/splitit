package com.splitit.domain.repository

import com.splitit.domain.model.Settlement
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.SettlementId

interface SettlementRepository {
    suspend fun getLatestSettlement(groupId: GroupId): Settlement?
    suspend fun getSettlement(id: SettlementId): Settlement?
    suspend fun saveSettlement(settlement: Settlement)
    suspend fun deleteSettlements(groupId: GroupId)
}
