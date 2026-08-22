package com.splitit.testutils

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.model.Settlement
import com.splitit.domain.repository.AppSettings
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.repository.SettingsRepository
import com.splitit.domain.repository.SettlementRepository
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.SettlementId

class InMemoryGroupRepository(
    initialGroups: Iterable<ExpenseGroup> = emptyList(),
) : GroupRepository {
    private val groups = initialGroups.associateBy { it.id }.toMutableMap()

    var getError: Throwable? = null
    var saveError: Throwable? = null
    var deleteError: Throwable? = null
    var saveCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    val savedGroups: List<ExpenseGroup>
        get() = groups.values.toList()

    override suspend fun getGroups(): List<ExpenseGroup> {
        getError?.let { throw it }
        return groups.values.toList()
    }

    override suspend fun getGroup(id: GroupId): ExpenseGroup? {
        getError?.let { throw it }
        return groups[id]
    }

    override suspend fun saveGroup(group: ExpenseGroup) {
        saveError?.let { throw it }
        saveCalls++
        groups[group.id] = group
    }

    override suspend fun deleteGroup(id: GroupId) {
        deleteError?.let { throw it }
        deleteCalls++
        groups.remove(id)
    }
}

class InMemoryParticipantRepository(
    initialParticipants: Iterable<Participant> = emptyList(),
) : ParticipantRepository {
    private val participants = initialParticipants.associateBy { it.id }.toMutableMap()
    val usedParticipantIds: MutableSet<ParticipantId> = mutableSetOf()

    var getError: Throwable? = null
    var saveError: Throwable? = null
    var deleteError: Throwable? = null
    var saveCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    val savedParticipants: List<Participant>
        get() = participants.values.toList()

    override suspend fun getParticipants(groupId: GroupId): List<Participant> {
        getError?.let { throw it }
        return participants.values.filter { it.groupId == groupId }
    }

    override suspend fun getParticipant(id: ParticipantId): Participant? {
        getError?.let { throw it }
        return participants[id]
    }

    override suspend fun saveParticipant(participant: Participant) {
        saveError?.let { throw it }
        saveCalls++
        participants[participant.id] = participant
    }

    override suspend fun deleteParticipant(id: ParticipantId) {
        deleteError?.let { throw it }
        deleteCalls++
        participants.remove(id)
    }

    override suspend fun isParticipantUsedByExpenses(id: ParticipantId): Boolean {
        getError?.let { throw it }
        return id in usedParticipantIds
    }
}

class InMemoryExpenseRepository(
    initialExpenses: Iterable<Expense> = emptyList(),
) : ExpenseRepository {
    private val expenses = initialExpenses.associateBy { it.id }.toMutableMap()

    var getError: Throwable? = null
    var saveError: Throwable? = null
    var deleteError: Throwable? = null
    var saveCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    val savedExpenses: List<Expense>
        get() = expenses.values.toList()

    override suspend fun getExpenses(groupId: GroupId): List<Expense> {
        getError?.let { throw it }
        return expenses.values.filter { it.groupId == groupId }
    }

    override suspend fun getExpense(id: ExpenseId): Expense? {
        getError?.let { throw it }
        return expenses[id]
    }

    override suspend fun saveExpense(expense: Expense) {
        saveError?.let { throw it }
        saveCalls++
        expenses[expense.id] = expense
    }

    override suspend fun deleteExpense(id: ExpenseId) {
        deleteError?.let { throw it }
        deleteCalls++
        expenses.remove(id)
    }
}

class InMemorySettlementRepository(
    initialSettlements: Iterable<Settlement> = emptyList(),
) : SettlementRepository {
    private val settlements = initialSettlements.associateBy { it.id }.toMutableMap()

    var getError: Throwable? = null
    var saveError: Throwable? = null
    var deleteError: Throwable? = null
    var saveCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    val savedSettlements: List<Settlement>
        get() = settlements.values.toList()

    override suspend fun getLatestSettlement(groupId: GroupId): Settlement? {
        getError?.let { throw it }
        return settlements.values
            .filter { it.groupId == groupId }
            .maxWithOrNull(compareBy<Settlement> { it.generatedAtMillis }.thenBy { it.id.value })
    }

    override suspend fun getSettlement(id: SettlementId): Settlement? {
        getError?.let { throw it }
        return settlements[id]
    }

    override suspend fun saveSettlement(settlement: Settlement) {
        saveError?.let { throw it }
        saveCalls++
        settlements[settlement.id] = settlement
    }

    override suspend fun deleteSettlements(groupId: GroupId) {
        deleteError?.let { throw it }
        deleteCalls++
        settlements.values.removeAll { it.groupId == groupId }
    }
}

class InMemorySettingsRepository(
    initialSettings: AppSettings = AppSettings(),
) : SettingsRepository {
    var settings: AppSettings = initialSettings
        private set
    var getError: Throwable? = null
    var saveError: Throwable? = null
    var saveCalls: Int = 0
        private set

    override suspend fun getSettings(): AppSettings {
        getError?.let { throw it }
        return settings
    }

    override suspend fun saveSettings(settings: AppSettings) {
        saveError?.let { throw it }
        saveCalls++
        this.settings = settings
    }
}
