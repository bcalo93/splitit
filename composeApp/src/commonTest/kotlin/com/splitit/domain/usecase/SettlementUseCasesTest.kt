package com.splitit.domain.usecase

import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseParticipantShare
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.model.GroupStatus
import com.splitit.domain.model.Settlement
import com.splitit.domain.optimizer.ComposedOptimizer
import com.splitit.domain.optimizer.CycleOptimizer
import com.splitit.domain.optimizer.TransitiveOptimizer
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.repository.SettlementRepository
import com.splitit.domain.service.BalanceCalculator
import com.splitit.domain.service.SourceRevisionCalculator
import com.splitit.domain.value.Clock
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.IdGenerator
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
import kotlin.test.assertTrue

class SettlementUseCasesTest {
    private val groupId = GroupId("group")
    private val aliceId = ParticipantId("alice")
    private val bobId = ParticipantId("bob")
    private val expenseId = ExpenseId("expense")
    private val participants = mutableListOf(participant(aliceId), participant(bobId))
    private val expenses = mutableListOf(expense())

    @Test
    fun generatesOptimizedSettlementAndPersistsIt() = runSuspendingTest {
        val settlementRepository = InMemorySettlementRepository()
        val useCase = generateUseCase(settlementRepository)

        val settlement = useCase.invoke(GenerateSettlementParams(groupId))

        assertEquals(settlement, settlementRepository.saved)
        assertEquals(
            SourceRevisionCalculator.calculate(participants, expenses),
            settlement.sourceRevision,
        )
        assertEquals(1, settlement.transfers.size)
        assertEquals(bobId, settlement.transfers.first().fromParticipantId)
        assertEquals(aliceId, settlement.transfers.first().toParticipantId)
        assertEquals(Money(500, "USD"), settlement.transfers.first().amount)
    }

    @Test
    fun detailsMarkSettlementStaleWhenSourceChangesWithoutTimestampChange() = runSuspendingTest {
        val settlementRepository = InMemorySettlementRepository()
        val useCase = generateUseCase(settlementRepository)
        useCase.invoke(GenerateSettlementParams(groupId))

        val groupRepository = InMemoryGroupRepository(group())
        val observeDetails = ObserveGroupDetailsUseCase(
            groupRepository = groupRepository,
            participantRepository = InMemoryParticipantRepository(participants),
            expenseRepository = InMemoryExpenseRepository(expenses),
            settlementRepository = settlementRepository,
        )

        assertFalse(observeDetails.invoke(ObserveGroupDetailsParams(groupId)).isSettlementStale)

        expenses[0] = expenses[0].copy(title = "Updated dinner", updatedAtMillis = 1)

        assertTrue(observeDetails.invoke(ObserveGroupDetailsParams(groupId)).isSettlementStale)
    }

    private fun generateUseCase(settlementRepository: InMemorySettlementRepository): GenerateSettlementUseCase {
        return GenerateSettlementUseCase(
            participantRepository = InMemoryParticipantRepository(participants),
            expenseRepository = InMemoryExpenseRepository(expenses),
            settlementRepository = settlementRepository,
            balanceCalculator = BalanceCalculator(),
            optimizer = ComposedOptimizer(listOf(CycleOptimizer(), TransitiveOptimizer())),
            idGenerator = TestIdGenerator,
            clock = FixedClock,
        )
    }

    private fun group(): ExpenseGroup {
        return ExpenseGroup(
            id = groupId,
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
            groupId = groupId,
            name = id.value,
            avatarColor = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )
    }

    private fun expense(): Expense {
        return Expense(
            id = expenseId,
            groupId = groupId,
            title = "Dinner",
            amount = Money(1000, "USD"),
            payerId = aliceId,
            participantShares = listOf(
                ExpenseParticipantShare(expenseId, aliceId, 500L),
                ExpenseParticipantShare(expenseId, bobId, 500L),
            ),
            dateMillis = 1,
            note = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )
    }

    private object FixedClock : Clock {
        override fun nowMillis(): Long = 10
    }

    private object TestIdGenerator : IdGenerator {
        override fun newGroupId(): GroupId = GroupId("group")
        override fun newParticipantId(): ParticipantId = ParticipantId("participant")
        override fun newExpenseId(): ExpenseId = ExpenseId("expense-new")
        override fun newSettlementId(): SettlementId = SettlementId("settlement")
        override fun newTransferId(): TransferId = TransferId("transfer")
    }
}

private class InMemoryGroupRepository(
    private var group: ExpenseGroup,
) : GroupRepository {
    override suspend fun getGroups(): List<ExpenseGroup> = listOf(group)

    override suspend fun getGroup(id: GroupId): ExpenseGroup? {
        return group.takeIf { it.id == id }
    }

    override suspend fun saveGroup(group: ExpenseGroup) {
        this.group = group
    }

    override suspend fun deleteGroup(id: GroupId) {
        if (group.id == id) group = group.copy(status = GroupStatus.Archived)
    }
}

private class InMemoryParticipantRepository(
    private val participants: MutableList<Participant>,
) : ParticipantRepository {
    override suspend fun getParticipants(groupId: GroupId): List<Participant> {
        return participants.filter { it.groupId == groupId }
    }

    override suspend fun getParticipant(id: ParticipantId): Participant? {
        return participants.firstOrNull { it.id == id }
    }

    override suspend fun saveParticipant(participant: Participant) {
        participants.removeAll { it.id == participant.id }
        participants += participant
    }

    override suspend fun deleteParticipant(id: ParticipantId) {
        participants.removeAll { it.id == id }
    }

    override suspend fun isParticipantUsedByExpenses(id: ParticipantId): Boolean = false
}

private class InMemoryExpenseRepository(
    private val expenses: MutableList<Expense>,
) : ExpenseRepository {
    override suspend fun getExpenses(groupId: GroupId): List<Expense> {
        return expenses.filter { it.groupId == groupId }
    }

    override suspend fun getExpense(id: ExpenseId): Expense? {
        return expenses.firstOrNull { it.id == id }
    }

    override suspend fun saveExpense(expense: Expense) {
        expenses.removeAll { it.id == expense.id }
        expenses += expense
    }

    override suspend fun deleteExpense(id: ExpenseId) {
        expenses.removeAll { it.id == id }
    }
}

private class InMemorySettlementRepository : SettlementRepository {
    var saved: Settlement? = null
        private set

    override suspend fun getLatestSettlement(groupId: GroupId): Settlement? {
        return saved?.takeIf { it.groupId == groupId }
    }

    override suspend fun getSettlement(id: SettlementId): Settlement? {
        return saved?.takeIf { it.id == id }
    }

    override suspend fun saveSettlement(settlement: Settlement) {
        saved = settlement
    }

    override suspend fun deleteSettlements(groupId: GroupId) {
        if (saved?.groupId == groupId) saved = null
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
