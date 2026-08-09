package com.splitit.domain.usecase

import com.splitit.domain.model.SessionStatus
import com.splitit.domain.repository.AppSettings
import com.splitit.domain.repository.ThemeMode
import com.splitit.domain.value.Money
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemorySessionRepository
import com.splitit.testutils.InMemorySettingsRepository
import com.splitit.testutils.TestClock
import com.splitit.testutils.TestIdGenerator
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.participant
import com.splitit.testutils.session
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CrudUseCasesTest {
    @Test
    fun createSessionTrimsFieldsAndPersistsIt() = runTest {
        val repository = InMemorySessionRepository()
        val session = CreateSessionUseCase(
            sessionRepository = repository,
            idGenerator = TestIdGenerator(),
            clock = TestClock(20L),
        )(
            title = "  Weekend trip  ",
            description = "  Shared costs  ",
        )

        assertEquals("Weekend trip", session.title)
        assertEquals("Shared costs", session.description)
        assertEquals(20L, session.createdAtMillis)
        assertEquals(listOf(session), repository.savedSessions)
    }

    @Test
    fun updateSessionPreservesIdentityCreationAndStatus() = runTest {
        val original = session(
            createdAtMillis = 1L,
            updatedAtMillis = 1L,
            status = SessionStatus.Archived,
        )
        val repository = InMemorySessionRepository(listOf(original))

        val updated = UpdateSessionUseCase(repository, TestClock(20L))(
            sessionId = original.id,
            title = "  New title ",
            description = " ",
        )

        assertEquals(original.id, updated.id)
        assertEquals(original.createdAtMillis, updated.createdAtMillis)
        assertEquals(SessionStatus.Archived, updated.status)
        assertEquals("New title", updated.title)
        assertEquals(null, updated.description)
        assertEquals(20L, updated.updatedAtMillis)
    }

    @Test
    fun updateSessionFailsBeforeSavingWhenSessionDoesNotExist() = runTest {
        val repository = InMemorySessionRepository()

        assertFailsWith<IllegalArgumentException> {
            UpdateSessionUseCase(repository, TestClock())(
                sessionId = TestIds.session,
                title = "Trip",
                description = null,
            )
        }

        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun addParticipantRequiresSessionAndTrimsName() = runTest {
        val sessionRepository = InMemorySessionRepository(listOf(session()))
        val participantRepository = InMemoryParticipantRepository()
        val useCase = AddParticipantUseCase(
            sessionRepository = sessionRepository,
            participantRepository = participantRepository,
            idGenerator = TestIdGenerator(participantId = TestIds.charlie),
            clock = TestClock(30L),
        )

        val participant = useCase(TestIds.session, "  Charlie  ", "#123456")

        assertEquals("Charlie", participant.name)
        assertEquals(30L, participant.createdAtMillis)
        assertEquals(listOf(participant), participantRepository.savedParticipants)

        val missingSessionRepository = InMemoryParticipantRepository()
        assertFailsWith<IllegalArgumentException> {
            AddParticipantUseCase(
                sessionRepository = InMemorySessionRepository(),
                participantRepository = missingSessionRepository,
                idGenerator = TestIdGenerator(),
                clock = TestClock(),
            )(TestIds.session, "Alice", null)
        }
        assertEquals(0, missingSessionRepository.saveCalls)
    }

    @Test
    fun removeParticipantBlocksUsedParticipantsAndAllowsUnusedOnes() = runTest {
        val repository = InMemoryParticipantRepository(listOf(participant()))
        repository.usedParticipantIds += TestIds.alice

        assertFailsWith<IllegalArgumentException> {
            RemoveParticipantUseCase(repository)(TestIds.alice)
        }
        assertEquals(0, repository.deleteCalls)

        repository.usedParticipantIds.clear()
        RemoveParticipantUseCase(repository)(TestIds.alice)
        assertEquals(1, repository.deleteCalls)
        assertEquals(emptyList(), repository.savedParticipants)
    }

    @Test
    fun createExpenseTrimsFieldsAndDeduplicatesParticipantIds() = runTest {
        val sessionRepository = InMemorySessionRepository(listOf(session()))
        val participantRepository = InMemoryParticipantRepository(
            listOf(participant(TestIds.alice), participant(TestIds.bob)),
        )
        val expenseRepository = InMemoryExpenseRepository()

        val created = CreateExpenseUseCase(
            sessionRepository = sessionRepository,
            participantRepository = participantRepository,
            expenseRepository = expenseRepository,
            idGenerator = TestIdGenerator(),
            clock = TestClock(40L),
        )(
            sessionId = TestIds.session,
            title = "  Dinner ",
            amount = Money(1_250L, "EUR"),
            payerId = TestIds.alice,
            participantIds = listOf(TestIds.alice, TestIds.bob, TestIds.alice),
            dateMillis = 2L,
            note = "  With dessert  ",
        )

        assertEquals("Dinner", created.title)
        assertEquals("With dessert", created.note)
        assertEquals(2, created.participantShares.size)
        assertEquals(Money(1_250L, "EUR"), created.amount)
        assertEquals(40L, created.updatedAtMillis)
        assertEquals(listOf(created), expenseRepository.savedExpenses)
    }

    @Test
    fun createExpenseRejectsInvalidParticipantsBeforeSaving() = runTest {
        val sessionRepository = InMemorySessionRepository(listOf(session()))
        val participantRepository = InMemoryParticipantRepository(listOf(participant()))
        val repository = InMemoryExpenseRepository()
        val useCase = CreateExpenseUseCase(
            sessionRepository,
            participantRepository,
            repository,
            TestIdGenerator(),
            TestClock(),
        )

        assertFailsWith<IllegalArgumentException> {
            useCase(
                sessionId = TestIds.session,
                title = "Dinner",
                amount = Money(100L, "USD"),
                payerId = TestIds.bob,
                participantIds = listOf(TestIds.alice),
                dateMillis = 1L,
                note = null,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            useCase(
                sessionId = TestIds.session,
                title = "Dinner",
                amount = Money(100L, "USD"),
                payerId = TestIds.alice,
                participantIds = emptyList(),
                dateMillis = 1L,
                note = null,
            )
        }
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun updateExpensePreservesIdentityAndSession() = runTest {
        val original = expense()
        val expenseRepository = InMemoryExpenseRepository(listOf(original))
        val participantRepository = InMemoryParticipantRepository(
            listOf(participant(TestIds.alice), participant(TestIds.bob), participant(TestIds.charlie)),
        )

        val updated = UpdateExpenseUseCase(
            participantRepository = participantRepository,
            expenseRepository = expenseRepository,
            clock = TestClock(50L),
        )(
            expenseId = original.id,
            title = "  Lunch ",
            amount = Money(2_000L, "USD"),
            payerId = TestIds.bob,
            participantIds = listOf(TestIds.bob, TestIds.charlie),
            dateMillis = 8L,
            note = "  Office  ",
        )

        assertEquals(original.id, updated.id)
        assertEquals(original.sessionId, updated.sessionId)
        assertEquals(original.createdAtMillis, updated.createdAtMillis)
        assertEquals("Lunch", updated.title)
        assertEquals("Office", updated.note)
        assertEquals(50L, updated.updatedAtMillis)
        assertEquals(listOf(updated), expenseRepository.savedExpenses)
    }

    @Test
    fun settingsUseCasesReadAndPersistSettings() = runTest {
        val repository = InMemorySettingsRepository()
        val settings = AppSettings(defaultCurrencyCode = "EUR", themeMode = ThemeMode.Dark)

        assertEquals(AppSettings(), GetSettingsUseCase(repository)())
        SaveSettingsUseCase(repository)(settings)

        assertEquals(settings, GetSettingsUseCase(repository)())
        assertEquals(1, repository.saveCalls)
    }
}
