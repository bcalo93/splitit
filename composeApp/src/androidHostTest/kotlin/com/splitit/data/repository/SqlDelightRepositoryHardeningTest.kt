package com.splitit.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.splitit.data.database.SplitItDatabase
import com.splitit.domain.repository.ThemeMode
import com.splitit.domain.value.GroupId
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.participant
import com.splitit.testutils.group
import com.splitit.testutils.settlement
import com.splitit.testutils.share
import com.splitit.testutils.transfer
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SqlDelightRepositoryHardeningTest {
    @Test
    fun groupsAreOrderedNewestFirstAndIsolatedById() = runSuspendingTest {
        val repositories = testRepositories()
        val first = group(id = TestIds.group, createdAtMillis = 1L)
        val second = group(id = TestIds.secondGroup, createdAtMillis = 2L)
        repositories.groupRepository.saveGroup(first)
        repositories.groupRepository.saveGroup(second)

        assertEquals(listOf(second, first), repositories.groupRepository.getGroups())
        assertEquals(first, repositories.groupRepository.getGroup(first.id))
        assertNull(repositories.groupRepository.getGroup(GroupId("missing")))
    }

    @Test
    fun updatingGroupPreservesParticipantsExpensesAndSettlements() = runSuspendingTest {
        val repositories = testRepositories()
        val originalGroup = group()
        val alice = participant(TestIds.alice)
        val bob = participant(TestIds.bob)
        val savedExpense = expense()
        val savedSettlement = settlement(
            transfers = listOf(transfer()),
        )
        repositories.groupRepository.saveGroup(originalGroup)
        repositories.participantRepository.saveParticipant(alice)
        repositories.participantRepository.saveParticipant(bob)
        repositories.expenseRepository.saveExpense(savedExpense)
        repositories.settlementRepository.saveSettlement(savedSettlement)

        repositories.groupRepository.saveGroup(
            originalGroup.copy(title = "Updated", updatedAtMillis = 2L),
        )

        assertEquals(listOf(alice, bob), repositories.participantRepository.getParticipants(originalGroup.id))
        assertEquals(savedExpense, repositories.expenseRepository.getExpense(savedExpense.id))
        assertEquals(savedSettlement, repositories.settlementRepository.getLatestSettlement(originalGroup.id))
        assertEquals("Updated", repositories.groupRepository.getGroup(originalGroup.id)?.title)
    }

    @Test
    fun participantsAndExpensesUseTheirDeclaredOrdering() = runSuspendingTest {
        val repositories = testRepositories()
        repositories.groupRepository.saveGroup(group())
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
            repositories.participantRepository.getParticipants(TestIds.group),
        )
        assertEquals(
            listOf(newerExpense, olderExpense),
            repositories.expenseRepository.getExpenses(TestIds.group),
        )
    }

    @Test
    fun expenseSaveRollsBackHeaderAndSharesWhenForeignKeyFails() = runSuspendingTest {
        val repositories = testRepositories()
        val savedGroup = group()
        val alice = participant(TestIds.alice)
        val original = expense(participantIds = listOf(TestIds.alice))
        repositories.groupRepository.saveGroup(savedGroup)
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
        val savedGroup = group()
        repositories.groupRepository.saveGroup(savedGroup)
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
        repositories.groupRepository.saveGroup(group())
        val first = settlement(id = TestIds.settlement, generatedAtMillis = 10L)
        val second = settlement(id = TestIds.secondSettlement, generatedAtMillis = 10L)
        repositories.settlementRepository.saveSettlement(first)
        repositories.settlementRepository.saveSettlement(second)

        assertEquals(second, repositories.settlementRepository.getLatestSettlement(TestIds.group))
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
        repositories.groupRepository.saveGroup(group())
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
        repositories.groupRepository.saveGroup(group())
        repositories.participantRepository.saveParticipant(participant(TestIds.alice))
        repositories.participantRepository.saveParticipant(participant(TestIds.bob))
        val savedSettlement = settlement(transfers = listOf(transfer()))
        repositories.settlementRepository.saveSettlement(savedSettlement)

        repositories.settlementRepository.deleteSettlements(TestIds.group)

        assertNull(repositories.settlementRepository.getSettlement(savedSettlement.id))
        assertNull(repositories.settlementRepository.getLatestSettlement(TestIds.group))
    }

    private fun testRepositories(): TestRepositories {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(null, "PRAGMA foreign_keys = ON", 0)
        SplitItDatabase.Schema.create(driver)
        val database = SplitItDatabase(driver)
        return TestRepositories(
            database = database,
            groupRepository = SqlDelightGroupRepository(database),
            participantRepository = SqlDelightParticipantRepository(database),
            expenseRepository = SqlDelightExpenseRepository(database),
            settlementRepository = SqlDelightSettlementRepository(database),
            settingsRepository = SqlDelightSettingsRepository(database),
        )
    }

    private data class TestRepositories(
        val database: SplitItDatabase,
        val groupRepository: SqlDelightGroupRepository,
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
