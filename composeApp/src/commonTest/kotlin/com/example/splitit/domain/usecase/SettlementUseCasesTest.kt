package com.example.splitit.domain.usecase

import com.example.splitit.domain.model.Expense
import com.example.splitit.domain.model.ExpenseParticipantShare
import com.example.splitit.domain.model.ExpenseSession
import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.model.SessionStatus
import com.example.splitit.domain.model.Settlement
import com.example.splitit.domain.optimizer.PaymentOptimizerAdapter
import com.example.splitit.domain.repository.ExpenseRepository
import com.example.splitit.domain.repository.ParticipantRepository
import com.example.splitit.domain.repository.SessionRepository
import com.example.splitit.domain.repository.SettlementRepository
import com.example.splitit.domain.service.BalanceCalculator
import com.example.splitit.domain.service.SourceRevisionCalculator
import com.example.splitit.domain.value.Clock
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.IdGenerator
import com.example.splitit.domain.value.Money
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import com.example.splitit.domain.value.SettlementId
import com.example.splitit.domain.value.TransferId
import com.example.splitit.logic.optimizers.ComposedOptimizer
import com.example.splitit.logic.optimizers.debt.CycleOptimizer
import com.example.splitit.logic.optimizers.debt.TransitiveOptimizer
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettlementUseCasesTest {
    private val sessionId = SessionId("session")
    private val aliceId = ParticipantId("alice")
    private val bobId = ParticipantId("bob")
    private val expenseId = ExpenseId("expense")
    private val participants = mutableListOf(participant(aliceId), participant(bobId))
    private val expenses = mutableListOf(expense())

    @Test
    fun generatesOptimizedSettlementAndPersistsIt() = runSuspendingTest {
        val settlementRepository = InMemorySettlementRepository()
        val useCase = generateUseCase(settlementRepository)

        val settlement = useCase(sessionId)

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
        useCase(sessionId)

        val sessionRepository = InMemorySessionRepository(session())
        val observeDetails = ObserveSessionDetailsUseCase(
            sessionRepository = sessionRepository,
            participantRepository = InMemoryParticipantRepository(participants),
            expenseRepository = InMemoryExpenseRepository(expenses),
            settlementRepository = settlementRepository,
        )

        assertFalse(observeDetails(sessionId).isSettlementStale)

        expenses[0] = expenses[0].copy(title = "Updated dinner", updatedAtMillis = 1)

        assertTrue(observeDetails(sessionId).isSettlementStale)
    }

    private fun generateUseCase(settlementRepository: InMemorySettlementRepository): GenerateSettlementUseCase {
        return GenerateSettlementUseCase(
            participantRepository = InMemoryParticipantRepository(participants),
            expenseRepository = InMemoryExpenseRepository(expenses),
            settlementRepository = settlementRepository,
            balanceCalculator = BalanceCalculator(),
            optimizerAdapter = PaymentOptimizerAdapter(
                optimizer = ComposedOptimizer(listOf(CycleOptimizer(), TransitiveOptimizer())),
                idGenerator = TestIdGenerator,
            ),
            idGenerator = TestIdGenerator,
            clock = FixedClock,
        )
    }

    private fun session(): ExpenseSession {
        return ExpenseSession(
            id = sessionId,
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
            sessionId = sessionId,
            name = id.value,
            avatarColor = null,
            createdAtMillis = 1,
            updatedAtMillis = 1,
        )
    }

    private fun expense(): Expense {
        return Expense(
            id = expenseId,
            sessionId = sessionId,
            title = "Dinner",
            amount = Money(1000, "USD"),
            payerId = aliceId,
            participantShares = listOf(
                ExpenseParticipantShare(expenseId, aliceId),
                ExpenseParticipantShare(expenseId, bobId),
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
        override fun newSessionId(): SessionId = SessionId("session")
        override fun newParticipantId(): ParticipantId = ParticipantId("participant")
        override fun newExpenseId(): ExpenseId = ExpenseId("expense-new")
        override fun newSettlementId(): SettlementId = SettlementId("settlement")
        override fun newTransferId(): TransferId = TransferId("transfer")
    }
}

private class InMemorySessionRepository(
    private var session: ExpenseSession,
) : SessionRepository {
    override suspend fun getSessions(): List<ExpenseSession> = listOf(session)

    override suspend fun getSession(id: SessionId): ExpenseSession? {
        return session.takeIf { it.id == id }
    }

    override suspend fun saveSession(session: ExpenseSession) {
        this.session = session
    }

    override suspend fun deleteSession(id: SessionId) {
        if (session.id == id) session = session.copy(status = SessionStatus.Archived)
    }
}

private class InMemoryParticipantRepository(
    private val participants: MutableList<Participant>,
) : ParticipantRepository {
    override suspend fun getParticipants(sessionId: SessionId): List<Participant> {
        return participants.filter { it.sessionId == sessionId }
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
    override suspend fun getExpenses(sessionId: SessionId): List<Expense> {
        return expenses.filter { it.sessionId == sessionId }
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

    override suspend fun getLatestSettlement(sessionId: SessionId): Settlement? {
        return saved?.takeIf { it.sessionId == sessionId }
    }

    override suspend fun getSettlement(id: SettlementId): Settlement? {
        return saved?.takeIf { it.id == id }
    }

    override suspend fun saveSettlement(settlement: Settlement) {
        saved = settlement
    }

    override suspend fun deleteSettlements(sessionId: SessionId) {
        if (saved?.sessionId == sessionId) saved = null
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
