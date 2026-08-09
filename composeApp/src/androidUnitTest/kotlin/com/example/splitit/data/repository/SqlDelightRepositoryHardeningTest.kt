package com.example.splitit.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.splitit.data.database.SplitItDatabase
import com.example.splitit.domain.repository.ThemeMode
import com.example.splitit.domain.value.SessionId
import com.example.splitit.testutils.TestIds
import com.example.splitit.testutils.expense
import com.example.splitit.testutils.participant
import com.example.splitit.testutils.session
import com.example.splitit.testutils.settlement
import com.example.splitit.testutils.share
import com.example.splitit.testutils.transfer
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SqlDelightRepositoryHardeningTest {
    @Test
    fun sessionsAreOrderedNewestFirstAndIsolatedById() = runSuspendingTest {
        val repositories = testRepositories()
        val first = session(id = TestIds.session, createdAtMillis = 1L)
        val second = session(id = TestIds.secondSession, createdAtMillis = 2L)
        repositories.sessionRepository.saveSession(first)
        repositories.sessionRepository.saveSession(second)

        assertEquals(listOf(second, first), repositories.sessionRepository.getSessions())
        assertEquals(first, repositories.sessionRepository.getSession(first.id))
        assertNull(repositories.sessionRepository.getSession(SessionId("missing")))
    }

    @Test
    fun updatingSessionPreservesParticipantsExpensesAndSettlements() = runSuspendingTest {
        val repositories = testRepositories()
        val originalSession = session()
        val alice = participant(TestIds.alice)
        val bob = participant(TestIds.bob)
        val savedExpense = expense()
        val savedSettlement = settlement(
            transfers = listOf(transfer()),
        )
        repositories.sessionRepository.saveSession(originalSession)
        repositories.participantRepository.saveParticipant(alice)
        repositories.participantRepository.saveParticipant(bob)
        repositories.expenseRepository.saveExpense(savedExpense)
        repositories.settlementRepository.saveSettlement(savedSettlement)

        repositories.sessionRepository.saveSession(
            originalSession.copy(title = "Updated", updatedAtMillis = 2L),
        )

        assertEquals(listOf(alice, bob), repositories.participantRepository.getParticipants(originalSession.id))
        assertEquals(savedExpense, repositories.expenseRepository.getExpense(savedExpense.id))
        assertEquals(savedSettlement, repositories.settlementRepository.getLatestSettlement(originalSession.id))
        assertEquals("Updated", repositories.sessionRepository.getSession(originalSession.id)?.title)
    }

    @Test
    fun participantsAndExpensesUseTheirDeclaredOrdering() = runSuspendingTest {
        val repositories = testRepositories()
        repositories.sessionRepository.saveSession(session())
        val firstParticipant = participant(TestIds.alice, createdAtMillis = 2L)
        val secondParticipant = participant(TestIds.bob, createdAtMillis = 1L)
        repositories.participantRepository.saveParticipant(firstParticipant)
        repositories.participantRepository.saveParticipant(secondParticipant)

        val olderExpense = expense(
            id = TestIds.expense,
            dateMillis = 5L,
            createdAtMillis = 1L,
            participantIds = listOf(TestIds.alice),
        )
        val newerExpense = expense(
            id = TestIds.secondExpense,
            dateMillis = 5L,
            createdAtMillis = 2L,
            participantIds = listOf(TestIds.alice),
        )
        repositories.expenseRepository.saveExpense(olderExpense)
        repositories.expenseRepository.saveExpense(newerExpense)

        assertEquals(
            listOf(secondParticipant, firstParticipant),
            repositories.participantRepository.getParticipants(TestIds.session),
        )
        assertEquals(
            listOf(newerExpense, olderExpense),
            repositories.expenseRepository.getExpenses(TestIds.session),
        )
    }

    @Test
    fun expenseSaveRollsBackHeaderAndSharesWhenForeignKeyFails() = runSuspendingTest {
        val repositories = testRepositories()
        val savedSession = session()
        val alice = participant(TestIds.alice)
        val original = expense(participantIds = listOf(TestIds.alice))
        repositories.sessionRepository.saveSession(savedSession)
        repositories.participantRepository.saveParticipant(alice)
        repositories.expenseRepository.saveExpense(original)

        val invalid = original.copy(
            title = "Invalid update",
            participantShares = listOf(share(participantId = TestIds.charlie)),
        )
        assertFailsWith<Exception> {
            repositories.expenseRepository.saveExpense(invalid)
        }

        assertEquals(original, repositories.expenseRepository.getExpense(original.id))
    }

    @Test
    fun settlementSaveRollsBackHeaderAndTransfersWhenForeignKeyFails() = runSuspendingTest {
        val repositories = testRepositories()
        val savedSession = session()
        repositories.sessionRepository.saveSession(savedSession)
        repositories.participantRepository.saveParticipant(participant(TestIds.alice))
        repositories.participantRepository.saveParticipant(participant(TestIds.bob))
        val original = settlement(transfers = listOf(transfer()))
        repositories.settlementRepository.saveSettlement(original)

        val invalid = original.copy(
            sourceRevision = 2L,
            transfers = listOf(transfer(toParticipantId = TestIds.charlie)),
        )
        assertFailsWith<Exception> {
            repositories.settlementRepository.saveSettlement(invalid)
        }

        assertEquals(original, repositories.settlementRepository.getSettlement(original.id))
    }

    @Test
    fun latestSettlementUsesIdAsStableTieBreaker() = runSuspendingTest {
        val repositories = testRepositories()
        repositories.sessionRepository.saveSession(session())
        val first = settlement(id = TestIds.settlement, generatedAtMillis = 10L)
        val second = settlement(id = TestIds.secondSettlement, generatedAtMillis = 10L)
        repositories.settlementRepository.saveSettlement(first)
        repositories.settlementRepository.saveSettlement(second)

        assertEquals(second, repositories.settlementRepository.getLatestSettlement(TestIds.session))
    }

    @Test
    fun settingsFallbackToSystemForUnknownThemeValues() = runSuspendingTest {
        val repositories = testRepositories()
        repositories.database.splitItDatabaseQueries.upsertSettings(
            default_currency_code = "GBP",
            theme_mode = "Unknown",
        )

        val settings = repositories.settingsRepository.getSettings()

        assertEquals("GBP", settings.defaultCurrencyCode)
        assertEquals(ThemeMode.System, settings.themeMode)
    }

    @Test
    fun deletingExpenseRemovesSharesAndUsageReferences() = runSuspendingTest {
        val repositories = testRepositories()
        repositories.sessionRepository.saveSession(session())
        repositories.participantRepository.saveParticipant(participant(TestIds.alice))
        val savedExpense = expense(participantIds = listOf(TestIds.alice))
        repositories.expenseRepository.saveExpense(savedExpense)

        assertEquals(true, repositories.participantRepository.isParticipantUsedByExpenses(TestIds.alice))
        repositories.expenseRepository.deleteExpense(savedExpense.id)

        assertNull(repositories.expenseRepository.getExpense(savedExpense.id))
        assertEquals(false, repositories.participantRepository.isParticipantUsedByExpenses(TestIds.alice))
    }

    @Test
    fun deletingSettlementsRemovesTheirTransfers() = runSuspendingTest {
        val repositories = testRepositories()
        repositories.sessionRepository.saveSession(session())
        repositories.participantRepository.saveParticipant(participant(TestIds.alice))
        repositories.participantRepository.saveParticipant(participant(TestIds.bob))
        val savedSettlement = settlement(transfers = listOf(transfer()))
        repositories.settlementRepository.saveSettlement(savedSettlement)

        repositories.settlementRepository.deleteSettlements(TestIds.session)

        assertNull(repositories.settlementRepository.getSettlement(savedSettlement.id))
        assertNull(repositories.settlementRepository.getLatestSettlement(TestIds.session))
    }

    private fun testRepositories(): TestRepositories {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(null, "PRAGMA foreign_keys = ON", 0)
        SplitItDatabase.Schema.create(driver)
        val database = SplitItDatabase(driver)
        return TestRepositories(
            database = database,
            sessionRepository = SqlDelightSessionRepository(database),
            participantRepository = SqlDelightParticipantRepository(database),
            expenseRepository = SqlDelightExpenseRepository(database),
            settlementRepository = SqlDelightSettlementRepository(database),
            settingsRepository = SqlDelightSettingsRepository(database),
        )
    }

    private data class TestRepositories(
        val database: SplitItDatabase,
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
        object : kotlin.coroutines.Continuation<Unit> {
            override val context = kotlin.coroutines.EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                failure = result.exceptionOrNull()
            }
        },
    )
    failure?.let { throw it }
}
