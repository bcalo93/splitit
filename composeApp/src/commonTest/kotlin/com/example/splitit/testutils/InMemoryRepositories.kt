package com.example.splitit.testutils

import com.example.splitit.domain.model.Expense
import com.example.splitit.domain.model.ExpenseSession
import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.model.Settlement
import com.example.splitit.domain.repository.AppSettings
import com.example.splitit.domain.repository.ExpenseRepository
import com.example.splitit.domain.repository.ParticipantRepository
import com.example.splitit.domain.repository.SessionRepository
import com.example.splitit.domain.repository.SettingsRepository
import com.example.splitit.domain.repository.SettlementRepository
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import com.example.splitit.domain.value.SettlementId

class InMemorySessionRepository(
    initialSessions: Iterable<ExpenseSession> = emptyList(),
) : SessionRepository {
    private val sessions = initialSessions.associateBy { it.id }.toMutableMap()

    var getError: Throwable? = null
    var saveError: Throwable? = null
    var deleteError: Throwable? = null
    var saveCalls: Int = 0
        private set
    var deleteCalls: Int = 0
        private set

    val savedSessions: List<ExpenseSession>
        get() = sessions.values.toList()

    override suspend fun getSessions(): List<ExpenseSession> {
        getError?.let { throw it }
        return sessions.values.toList()
    }

    override suspend fun getSession(id: SessionId): ExpenseSession? {
        getError?.let { throw it }
        return sessions[id]
    }

    override suspend fun saveSession(session: ExpenseSession) {
        saveError?.let { throw it }
        saveCalls++
        sessions[session.id] = session
    }

    override suspend fun deleteSession(id: SessionId) {
        deleteError?.let { throw it }
        deleteCalls++
        sessions.remove(id)
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

    override suspend fun getParticipants(sessionId: SessionId): List<Participant> {
        getError?.let { throw it }
        return participants.values.filter { it.sessionId == sessionId }
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

    override suspend fun getExpenses(sessionId: SessionId): List<Expense> {
        getError?.let { throw it }
        return expenses.values.filter { it.sessionId == sessionId }
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

    override suspend fun getLatestSettlement(sessionId: SessionId): Settlement? {
        getError?.let { throw it }
        return settlements.values
            .filter { it.sessionId == sessionId }
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

    override suspend fun deleteSettlements(sessionId: SessionId) {
        deleteError?.let { throw it }
        deleteCalls++
        settlements.values.removeAll { it.sessionId == sessionId }
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
