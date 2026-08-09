package com.example.splitit.data.repository

import com.example.splitit.data.database.SplitItDatabase
import com.example.splitit.data.mapper.toDomain
import com.example.splitit.domain.model.Settlement
import com.example.splitit.domain.repository.SettlementRepository
import com.example.splitit.domain.value.SessionId
import com.example.splitit.domain.value.SettlementId

class SqlDelightSettlementRepository(
    private val database: SplitItDatabase,
) : SettlementRepository {
    private val queries = database.splitItDatabaseQueries

    override suspend fun getLatestSettlement(sessionId: SessionId): Settlement? {
        val settlement = queries.selectLatestSettlement(sessionId.value).executeAsOneOrNull()
            ?: return null
        return settlement.toDomain(
            transfers = queries.selectSettlementTransfers(settlement.id)
                .executeAsList()
                .map { it.toDomain() },
        )
    }

    override suspend fun getSettlement(id: SettlementId): Settlement? {
        val settlement = queries.selectSettlementById(id.value).executeAsOneOrNull() ?: return null
        return settlement.toDomain(
            transfers = queries.selectSettlementTransfers(settlement.id)
                .executeAsList()
                .map { it.toDomain() },
        )
    }

    override suspend fun saveSettlement(settlement: Settlement) {
        database.transaction {
            queries.upsertSettlement(
                id = settlement.id.value,
                session_id = settlement.sessionId.value,
                generated_at = settlement.generatedAtMillis,
                source_revision = settlement.sourceRevision,
            )
            queries.deleteSettlementTransfers(settlement.id.value)
            settlement.transfers.forEach { transfer ->
                queries.insertSettlementTransfer(
                    id = transfer.id.value,
                    settlement_id = transfer.settlementId.value,
                    from_participant_id = transfer.fromParticipantId.value,
                    to_participant_id = transfer.toParticipantId.value,
                    amount_minor = transfer.amount.minorUnits,
                    currency_code = transfer.amount.currencyCode,
                )
            }
        }
    }

    override suspend fun deleteSettlements(sessionId: SessionId) {
        database.transaction {
            queries.deleteSettlementTransfersBySession(sessionId.value)
            queries.deleteSettlementsBySession(sessionId.value)
        }
    }
}
