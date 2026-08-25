package com.splitit.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.splitit.data.database.SplitItDatabase
import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseParticipantShare
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.ExpenseType
import com.splitit.domain.model.Participant
import com.splitit.domain.model.GroupStatus
import com.splitit.domain.model.Settlement
import com.splitit.domain.model.SettlementTransfer
import com.splitit.domain.repository.AppSettings
import com.splitit.domain.repository.ThemeMode
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.SettlementId
import com.splitit.domain.value.TransferId
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
    fun persistGroupAndParticipants() = runSuspendingTest {
        val repositories = testRepositories()
        val group = group()
        val alice = participant(ParticipantId("alice"))
        val bob = participant(ParticipantId("bob"))

        repositories.groupRepository.saveGroup(group)
        repositories.participantRepository.saveParticipant(alice)
        repositories.participantRepository.saveParticipant(bob)

        val loadedGroup = repositories.groupRepository.getGroup(group.id)
        val participants = repositories.participantRepository.getParticipants(group.id)

        assertNotNull(loadedGroup)
        assertEquals(setOf(alice.id, bob.id), loadedGroup.participantIds)
        assertEquals(listOf(alice, bob), participants)
    }

    @Test
    fun persistExpenseWithParticipantSharesTransactionally() = runSuspendingTest {
        val repositories = testRepositories()
        val group = group()
        val alice = participant(ParticipantId("alice"))
        val bob = participant(ParticipantId("bob"))
        val expense = expense(
            shares = listOf(
                ExpenseParticipantShare(ExpenseId("expense"), alice.id),
                ExpenseParticipantShare(ExpenseId("expense"), bob.id),
            ),
        )

        repositories.groupRepository.saveGroup(group)
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
        val group = group()
        val alice = participant(ParticipantId("alice"))
        val bob = participant(ParticipantId("bob"))
        val charlie = participant(ParticipantId("charlie"))

        repositories.groupRepository.saveGroup(group)
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
        val group = group()
        val alice = participant(ParticipantId("alice"))
        val bob = participant(ParticipantId("bob"))
        val settlement = Settlement(
            id = SettlementId("settlement"),
            groupId = group.id,
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

        repositories.groupRepository.saveGroup(group)
        repositories.participantRepository.saveParticipant(alice)
        repositories.participantRepository.saveParticipant(bob)
        repositories.settlementRepository.saveSettlement(settlement)

        assertEquals(settlement, repositories.settlementRepository.getLatestSettlement(group.id))
        assertEquals(settlement, repositories.settlementRepository.getSettlement(settlement.id))
    }

    @Test
    fun persistTransferPaymentExpenseType() = runSuspendingTest {
        val repositories = testRepositories()
        val group = group()
        val alice = participant(ParticipantId("alice"))
        val bob = participant(ParticipantId("bob"))
        val payment = Expense(
            id = ExpenseId("payment"),
            groupId = group.id,
            title = "Payment: bob → alice",
            amount = Money(500, "USD"),
            payerId = bob.id,
            participantShares = listOf(
                ExpenseParticipantShare(ExpenseId("payment"), alice.id),
            ),
            dateMillis = 2,
            note = null,
            createdAtMillis = 2,
            updatedAtMillis = 2,
            type = ExpenseType.TRANSFER_PAYMENT,
        )

        repositories.groupRepository.saveGroup(group)
        repositories.participantRepository.saveParticipant(alice)
        repositories.participantRepository.saveParticipant(bob)
        repositories.expenseRepository.saveExpense(payment)

        val loaded = repositories.expenseRepository.getExpense(payment.id)

        assertEquals(payment, loaded)
        assertEquals(ExpenseType.TRANSFER_PAYMENT, loaded?.type)
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
    fun deleteGroupCascadesOwnedData() = runSuspendingTest {
        val repositories = testRepositories()
        val group = group()
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
            groupId = group.id,
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

        repositories.groupRepository.saveGroup(group)
        repositories.participantRepository.saveParticipant(alice)
        repositories.participantRepository.saveParticipant(bob)
        repositories.expenseRepository.saveExpense(savedExpense)
        repositories.settlementRepository.saveSettlement(savedSettlement)

        repositories.groupRepository.deleteGroup(group.id)

        assertNull(repositories.groupRepository.getGroup(group.id))
        assertEquals(emptyList(), repositories.participantRepository.getParticipants(group.id))
        assertEquals(emptyList(), repositories.expenseRepository.getExpenses(group.id))
        assertNull(repositories.settlementRepository.getLatestSettlement(group.id))
    }

    private fun testRepositories(): TestRepositories {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SplitItDatabase.Schema.create(driver)
        val database = SplitItDatabase(driver)

        return TestRepositories(
            groupRepository = SqlDelightGroupRepository(database),
            participantRepository = SqlDelightParticipantRepository(database),
            expenseRepository = SqlDelightExpenseRepository(database),
            settlementRepository = SqlDelightSettlementRepository(database),
            settingsRepository = SqlDelightSettingsRepository(database),
        )
    }

    private fun group(): ExpenseGroup {
        return ExpenseGroup(
            id = GroupId("group"),
            title = "Trip",
            description = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
            status = GroupStatus.Active,
        )
    }

    private fun participant(id: ParticipantId): Participant {
        return Participant(
            id = id,
            groupId = GroupId("group"),
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
            groupId = GroupId("group"),
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
        object : Continuation<Unit> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                failure = result.exceptionOrNull()
            }
        },
    )
    failure?.let { throw it }
}
