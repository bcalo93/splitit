package com.example.splitit.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.splitit.data.database.SplitItDatabase
import com.example.splitit.domain.model.Expense
import com.example.splitit.domain.model.ExpenseParticipantShare
import com.example.splitit.domain.model.ExpenseSession
import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.model.SessionStatus
import com.example.splitit.domain.model.Settlement
import com.example.splitit.domain.model.SettlementTransfer
import com.example.splitit.domain.repository.AppSettings
import com.example.splitit.domain.repository.ThemeMode
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.Money
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import com.example.splitit.domain.value.SettlementId
import com.example.splitit.domain.value.TransferId
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqlDelightRepositoryTest {
    @Test
    fun persistSessionAndParticipants() = runSuspendingTest {
        val repositories = testRepositories()
        val session = session()
        val alice = participant(ParticipantId("alice"))
        val bob = participant(ParticipantId("bob"))

        repositories.sessionRepository.saveSession(session)
        repositories.participantRepository.saveParticipant(alice)
        repositories.participantRepository.saveParticipant(bob)

        val loadedSession = repositories.sessionRepository.getSession(session.id)
        val participants = repositories.participantRepository.getParticipants(session.id)

        assertNotNull(loadedSession)
        assertEquals(setOf(alice.id, bob.id), loadedSession.participantIds)
        assertEquals(listOf(alice, bob), participants)
    }

    @Test
    fun persistExpenseWithParticipantSharesTransactionally() = runSuspendingTest {
        val repositories = testRepositories()
        val session = session()
        val alice = participant(ParticipantId("alice"))
        val bob = participant(ParticipantId("bob"))
        val expense = expense(
            shares = listOf(
                ExpenseParticipantShare(ExpenseId("expense"), alice.id),
                ExpenseParticipantShare(ExpenseId("expense"), bob.id),
            ),
        )

        repositories.sessionRepository.saveSession(session)
        repositories.participantRepository.saveParticipant(alice)
        repositories.participantRepository.saveParticipant(bob)
        repositories.expenseRepository.saveExpense(expense)

        val loaded = repositories.expenseRepository.getExpense(expense.id)

        assertEquals(expense, loaded)
        assertTrue(repositories.participantRepository.isParticipantUsedByExpenses(alice.id))
        assertTrue(repositories.participantRepository.isParticipantUsedByExpenses(bob.id))
    }

    @Test
    fun updateExpenseReplacesShares() = runSuspendingTest {
        val repositories = testRepositories()
        val session = session()
        val alice = participant(ParticipantId("alice"))
        val bob = participant(ParticipantId("bob"))
        val charlie = participant(ParticipantId("charlie"))

        repositories.sessionRepository.saveSession(session)
        listOf(alice, bob, charlie).forEach {
            repositories.participantRepository.saveParticipant(it)
        }

        val original = expense(
            shares = listOf(
                ExpenseParticipantShare(ExpenseId("expense"), alice.id),
                ExpenseParticipantShare(ExpenseId("expense"), bob.id),
            ),
        )
        val updated = original.copy(
            participantShares = listOf(
                ExpenseParticipantShare(ExpenseId("expense"), alice.id),
                ExpenseParticipantShare(ExpenseId("expense"), charlie.id),
            ),
            updatedAtMillis = 2,
        )

        repositories.expenseRepository.saveExpense(original)
        repositories.expenseRepository.saveExpense(updated)

        assertEquals(updated, repositories.expenseRepository.getExpense(original.id))
        assertFalse(repositories.participantRepository.isParticipantUsedByExpenses(bob.id))
        assertTrue(repositories.participantRepository.isParticipantUsedByExpenses(charlie.id))
    }

    @Test
    fun persistSettlementWithTransfers() = runSuspendingTest {
        val repositories = testRepositories()
        val session = session()
        val alice = participant(ParticipantId("alice"))
        val bob = participant(ParticipantId("bob"))
        val settlement = Settlement(
            id = SettlementId("settlement"),
            sessionId = session.id,
            generatedAtMillis = 10,
            sourceRevision = 5,
            transfers = listOf(
                SettlementTransfer(
                    id = TransferId("transfer"),
                    settlementId = SettlementId("settlement"),
                    fromParticipantId = bob.id,
                    toParticipantId = alice.id,
                    amount = Money(500, "USD"),
                ),
            ),
        )

        repositories.sessionRepository.saveSession(session)
        repositories.participantRepository.saveParticipant(alice)
        repositories.participantRepository.saveParticipant(bob)
        repositories.settlementRepository.saveSettlement(settlement)

        assertEquals(settlement, repositories.settlementRepository.getLatestSettlement(session.id))
        assertEquals(settlement, repositories.settlementRepository.getSettlement(settlement.id))
    }

    @Test
    fun persistSettings() = runSuspendingTest {
        val repositories = testRepositories()
        val settings = AppSettings(
            defaultCurrencyCode = "EUR",
            themeMode = ThemeMode.Dark,
        )

        assertEquals(AppSettings(), repositories.settingsRepository.getSettings())

        repositories.settingsRepository.saveSettings(settings)

        assertEquals(settings, repositories.settingsRepository.getSettings())
    }

    @Test
    fun deleteSessionCascadesOwnedData() = runSuspendingTest {
        val repositories = testRepositories()
        val session = session()
        val alice = participant(ParticipantId("alice"))
        val bob = participant(ParticipantId("bob"))
        val savedExpense = expense(
            shares = listOf(
                ExpenseParticipantShare(ExpenseId("expense"), alice.id),
                ExpenseParticipantShare(ExpenseId("expense"), bob.id),
            ),
        )
        val savedSettlement = Settlement(
            id = SettlementId("settlement"),
            sessionId = session.id,
            generatedAtMillis = 10,
            sourceRevision = 1,
            transfers = listOf(
                SettlementTransfer(
                    id = TransferId("transfer"),
                    settlementId = SettlementId("settlement"),
                    fromParticipantId = bob.id,
                    toParticipantId = alice.id,
                    amount = Money(500, "USD"),
                ),
            ),
        )

        repositories.sessionRepository.saveSession(session)
        repositories.participantRepository.saveParticipant(alice)
        repositories.participantRepository.saveParticipant(bob)
        repositories.expenseRepository.saveExpense(savedExpense)
        repositories.settlementRepository.saveSettlement(savedSettlement)

        repositories.sessionRepository.deleteSession(session.id)

        assertNull(repositories.sessionRepository.getSession(session.id))
        assertEquals(emptyList(), repositories.participantRepository.getParticipants(session.id))
        assertEquals(emptyList(), repositories.expenseRepository.getExpenses(session.id))
        assertNull(repositories.settlementRepository.getLatestSettlement(session.id))
    }

    private fun testRepositories(): TestRepositories {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SplitItDatabase.Schema.create(driver)
        val database = SplitItDatabase(driver)

        return TestRepositories(
            sessionRepository = SqlDelightSessionRepository(database),
            participantRepository = SqlDelightParticipantRepository(database),
            expenseRepository = SqlDelightExpenseRepository(database),
            settlementRepository = SqlDelightSettlementRepository(database),
            settingsRepository = SqlDelightSettingsRepository(database),
        )
    }

    private fun session(): ExpenseSession {
        return ExpenseSession(
            id = SessionId("session"),
            title = "Trip",
            description = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
            status = SessionStatus.Active,
        )
    }

    private fun participant(id: ParticipantId): Participant {
        return Participant(
            id = id,
            sessionId = SessionId("session"),
            name = id.value,
            avatarColor = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )
    }

    private fun expense(
        shares: List<ExpenseParticipantShare>,
    ): Expense {
        return Expense(
            id = ExpenseId("expense"),
            sessionId = SessionId("session"),
            title = "Dinner",
            amount = Money(1000, "USD"),
            payerId = ParticipantId("alice"),
            participantShares = shares,
            dateMillis = 1,
            note = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )
    }

    private data class TestRepositories(
        val sessionRepository: SqlDelightSessionRepository,
        val participantRepository: SqlDelightParticipantRepository,
        val expenseRepository: SqlDelightExpenseRepository,
        val settlementRepository: SqlDelightSettlementRepository,
        val settingsRepository: SqlDelightSettingsRepository,
    )
}

private fun runSuspendingTest(block: suspend () -> Unit) {
    var failure: Throwable? = null
    block.startCoroutine(
        object : Continuation<Unit> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                failure = result.exceptionOrNull()
            }
        },
    )
    failure?.let { throw it }
}
