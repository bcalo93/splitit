package com.splitit.data.repository

import com.splitit.data.database.SplitItDatabase
import com.splitit.data.mapper.toDomain
import com.splitit.domain.model.Settlement
import com.splitit.domain.repository.SettlementRepository
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.SettlementId

class SqlDelightSettlementRepository(
    private val database: SplitItDatabase,
) : SettlementRepository {
    private val queries = database.splitItDatabaseQueries

    override suspend fun getLatestSettlement(groupId: GroupId): Settlement? {
        val settlement = queries.selectLatestSettlement(groupId.value).executeAsOneOrNull()
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
                group_id = settlement.groupId.value,
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

    override suspend fun deleteSettlements(groupId: GroupId) {
        database.transaction {
            queries.deleteSettlementTransfersByGroup(groupId.value)
            queries.deleteSettlementsByGroup(groupId.value)
        }
    }
}
