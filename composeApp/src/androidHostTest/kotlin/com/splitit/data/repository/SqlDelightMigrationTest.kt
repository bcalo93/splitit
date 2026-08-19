package com.splitit.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.splitit.data.database.SplitItDatabase
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SettlementId
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlDelightMigrationTest {

    @Test
    fun migrationFromSessionsToGroupsPreservesData() = runSuspendingTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        V1_SCHEMA.forEach { statement -> driver.execute(null, statement, 0) }
        insertV1Data(driver)

        SplitItDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = 2)

        val database = SplitItDatabase(driver)
        val groupRepository = SqlDelightGroupRepository(database)
        val participantRepository = SqlDelightParticipantRepository(database)
        val expenseRepository = SqlDelightExpenseRepository(database)
        val settlementRepository = SqlDelightSettlementRepository(database)

        val groupId = GroupId("group-1")
        assertEquals("Trip", groupRepository.getGroup(groupId)?.title)
        assertEquals(
            listOf("alice", "bob"),
            participantRepository.getParticipants(groupId).map { it.id.value }.sorted(),
        )
        assertEquals(
            Money(1_000L, "USD"),
            expenseRepository.getExpenses(groupId).single().amount,
        )
        assertEquals(
            5L,
            settlementRepository.getLatestSettlement(groupId)?.sourceRevision,
        )
    }

    private fun insertV1Data(driver: JdbcSqliteDriver) {
        driver.execute(
            null,
            "INSERT INTO sessions(id, title, description, created_at, updated_at, status, deleted_at) " +
                "VALUES ('group-1', 'Trip', NULL, 1, 1, 'Active', NULL)",
            0,
        )
        driver.execute(
            null,
            "INSERT INTO participants(id, session_id, name, avatar_color, created_at, updated_at, deleted_at) " +
                "VALUES ('alice', 'group-1', 'Alice', NULL, 1, 1, NULL)",
            0,
        )
        driver.execute(
            null,
            "INSERT INTO participants(id, session_id, name, avatar_color, created_at, updated_at, deleted_at) " +
                "VALUES ('bob', 'group-1', 'Bob', NULL, 1, 1, NULL)",
            0,
        )
        driver.execute(
            null,
            "INSERT INTO expenses(id, session_id, title, amount_minor, currency_code, payer_participant_id, " +
                "date_millis, note, created_at, updated_at, deleted_at) " +
                "VALUES ('expense-1', 'group-1', 'Dinner', 1000, 'USD', 'alice', 1, NULL, 1, 1, NULL)",
            0,
        )
        driver.execute(
            null,
            "INSERT INTO expense_participants(expense_id, participant_id, share_weight) " +
                "VALUES ('expense-1', 'alice', 1), ('expense-1', 'bob', 1)",
            0,
        )
        driver.execute(
            null,
            "INSERT INTO settlements(id, session_id, generated_at, source_revision) " +
                "VALUES ('settlement-1', 'group-1', 10, 5)",
            0,
        )
    }

    private companion object {
        val V1_SCHEMA: List<String> = listOf(

            """
            CREATE TABLE sessions (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                status TEXT NOT NULL,
                deleted_at INTEGER
            )
            """.trimIndent(),
            """
            CREATE TABLE participants (
                id TEXT NOT NULL PRIMARY KEY,
                session_id TEXT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
                name TEXT NOT NULL,
                avatar_color TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                deleted_at INTEGER
            )
            """.trimIndent(),
            """
            CREATE TABLE expenses (
                id TEXT NOT NULL PRIMARY KEY,
                session_id TEXT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
                title TEXT NOT NULL,
                amount_minor INTEGER NOT NULL,
                currency_code TEXT NOT NULL,
                payer_participant_id TEXT NOT NULL REFERENCES participants(id),
                date_millis INTEGER NOT NULL,
                note TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                deleted_at INTEGER
            )
            """.trimIndent(),
            """
            CREATE TABLE expense_participants (
                expense_id TEXT NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
                participant_id TEXT NOT NULL REFERENCES participants(id),
                share_weight INTEGER NOT NULL,
                PRIMARY KEY(expense_id, participant_id)
            )
            """.trimIndent(),
            """
            CREATE TABLE settlements (
                id TEXT NOT NULL PRIMARY KEY,
                session_id TEXT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
                generated_at INTEGER NOT NULL,
                source_revision INTEGER NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE settlement_transfers (
                id TEXT NOT NULL PRIMARY KEY,
                settlement_id TEXT NOT NULL REFERENCES settlements(id) ON DELETE CASCADE,
                from_participant_id TEXT NOT NULL REFERENCES participants(id),
                to_participant_id TEXT NOT NULL REFERENCES participants(id),
                amount_minor INTEGER NOT NULL,
                currency_code TEXT NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE settings (
                id INTEGER NOT NULL PRIMARY KEY,
                default_currency_code TEXT NOT NULL,
                theme_mode TEXT NOT NULL
            )
            """.trimIndent(),
            "CREATE INDEX participants_session_id ON participants(session_id)",
            "CREATE INDEX expenses_session_id_date ON expenses(session_id, date_millis)",
            "CREATE INDEX expense_participants_expense_id ON expense_participants(expense_id)",
            "CREATE INDEX expense_participants_participant_id ON expense_participants(participant_id)",
            "CREATE INDEX settlements_session_id_generated_at ON settlements(session_id, generated_at)",
            "CREATE INDEX settlement_transfers_settlement_id ON settlement_transfers(settlement_id)",
        )
    }
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
