package com.splitit.domain.usecase

import com.splitit.domain.model.GroupStatus
import com.splitit.domain.repository.AppSettings
import com.splitit.domain.repository.ThemeMode
import com.splitit.domain.value.Money
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemoryGroupRepository
import com.splitit.testutils.InMemorySettingsRepository
import com.splitit.testutils.TestClock
import com.splitit.testutils.TestIdGenerator
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.participant
import com.splitit.testutils.group
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CrudUseCasesTest {
    @Test
    fun createGroupTrimsFieldsAndPersistsIt() = runTest {
        val repository = InMemoryGroupRepository()
        val group = CreateGroupUseCase(
            groupRepository = repository,
            idGenerator = TestIdGenerator(),
            clock = TestClock(20L),
        )(
            CreateGroupParams(
                title = "  Weekend trip  ",
                description = "  Shared costs  ",
            ),
        )

        assertEquals("Weekend trip", group.title)
        assertEquals("Shared costs", group.description)
        assertEquals(20L, group.createdAtMillis)
        assertEquals(listOf(group), repository.savedGroups)
    }

    @Test
    fun updateGroupPreservesIdentityCreationAndStatus() = runTest {
        val original = group(
            createdAtMillis = 1L,
            updatedAtMillis = 1L,
            status = GroupStatus.Archived,
        )
        val repository = InMemoryGroupRepository(listOf(original))

        val updated = UpdateGroupUseCase(repository, TestClock(20L))(
            UpdateGroupParams(
                groupId = original.id,
                title = "  New title ",
                description = " ",
            ),
        )

        assertEquals(original.id, updated.id)
        assertEquals(original.createdAtMillis, updated.createdAtMillis)
        assertEquals(GroupStatus.Archived, updated.status)
        assertEquals("New title", updated.title)
        assertEquals(null, updated.description)
        assertEquals(20L, updated.updatedAtMillis)
    }

    @Test
    fun updateGroupFailsBeforeSavingWhenGroupDoesNotExist() = runTest {
        val repository = InMemoryGroupRepository()

        assertFailsWith<IllegalArgumentException> {
            UpdateGroupUseCase(repository, TestClock())(
                UpdateGroupParams(
                    groupId = TestIds.group,
                    title = "Trip",
                    description = null,
                ),
            )
        }

        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun addParticipantRequiresGroupAndTrimsName() = runTest {
        val groupRepository = InMemoryGroupRepository(listOf(group()))
        val participantRepository = InMemoryParticipantRepository()
        val useCase = AddParticipantUseCase(
            groupRepository = groupRepository,
            participantRepository = participantRepository,
            idGenerator = TestIdGenerator(participantId = TestIds.charlie),
            clock = TestClock(30L),
        )

        val participant = useCase(
            AddParticipantParams(
                groupId = TestIds.group,
                name = "  Charlie  ",
                avatarColor = "#123456",
            ),
        )

        assertEquals("Charlie", participant.name)
        assertEquals(30L, participant.createdAtMillis)
        assertEquals(listOf(participant), participantRepository.savedParticipants)

        val missingGroupRepository = InMemoryParticipantRepository()
        assertFailsWith<IllegalArgumentException> {
            AddParticipantUseCase(
                groupRepository = InMemoryGroupRepository(),
                participantRepository = missingGroupRepository,
                idGenerator = TestIdGenerator(),
                clock = TestClock(),
            )(
                AddParticipantParams(
                    groupId = TestIds.group,
                    name = "Alice",
                    avatarColor = null,
                ),
            )
        }
        assertEquals(0, missingGroupRepository.saveCalls)
    }

    @Test
    fun removeParticipantBlocksUsedParticipantsAndAllowsUnusedOnes() = runTest {
        val repository = InMemoryParticipantRepository(listOf(participant()))
        repository.usedParticipantIds += TestIds.alice

        assertFailsWith<IllegalArgumentException> {
            RemoveParticipantUseCase(repository)(RemoveParticipantParams(TestIds.alice))
        }
        assertEquals(0, repository.deleteCalls)

        repository.usedParticipantIds.clear()
        RemoveParticipantUseCase(repository)(RemoveParticipantParams(TestIds.alice))
        assertEquals(1, repository.deleteCalls)
        assertEquals(emptyList(), repository.savedParticipants)
    }

    @Test
    fun createExpenseTrimsFieldsAndDeduplicatesParticipantIds() = runTest {
        val groupRepository = InMemoryGroupRepository(listOf(group()))
        val participantRepository = InMemoryParticipantRepository(
            listOf(participant(TestIds.alice), participant(TestIds.bob)),
        )
        val expenseRepository = InMemoryExpenseRepository()

        val created = CreateExpenseUseCase(
            groupRepository = groupRepository,
            participantRepository = participantRepository,
            expenseRepository = expenseRepository,
            idGenerator = TestIdGenerator(),
            clock = TestClock(40L),
        )(
            CreateExpenseParams(
                groupId = TestIds.group,
                title = "  Dinner ",
                amount = Money(1_250L, "EUR"),
                payerId = TestIds.alice,
                participantIds = listOf(TestIds.alice, TestIds.bob, TestIds.alice),
                dateMillis = 2L,
                note = "  With dessert  ",
            ),
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
        val groupRepository = InMemoryGroupRepository(listOf(group()))
        val participantRepository = InMemoryParticipantRepository(listOf(participant()))
        val repository = InMemoryExpenseRepository()
        val useCase = CreateExpenseUseCase(
            groupRepository,
            participantRepository,
            repository,
            TestIdGenerator(),
            TestClock(),
        )

        assertFailsWith<IllegalArgumentException> {
            useCase(
                CreateExpenseParams(
                    groupId = TestIds.group,
                    title = "Dinner",
                    amount = Money(100L, "USD"),
                    payerId = TestIds.bob,
                    participantIds = listOf(TestIds.alice),
                    dateMillis = 1L,
                    note = null,
                ),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            useCase(
                CreateExpenseParams(
                    groupId = TestIds.group,
                    title = "Dinner",
                    amount = Money(100L, "USD"),
                    payerId = TestIds.alice,
                    participantIds = emptyList(),
                    dateMillis = 1L,
                    note = null,
                ),
            )
        }
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun updateExpensePreservesIdentityAndGroup() = runTest {
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
            UpdateExpenseParams(
                expenseId = original.id,
                title = "  Lunch ",
                amount = Money(2_000L, "USD"),
                payerId = TestIds.bob,
                participantIds = listOf(TestIds.bob, TestIds.charlie),
                dateMillis = 8L,
                note = "  Office  ",
            ),
        )

        assertEquals(original.id, updated.id)
        assertEquals(original.groupId, updated.groupId)
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

        assertEquals(AppSettings(), GetSettingsUseCase(repository)(GetSettingsParams))
        SaveSettingsUseCase(repository)(SaveSettingsParams(settings))

        assertEquals(settings, GetSettingsUseCase(repository)(GetSettingsParams))
        assertEquals(1, repository.saveCalls)
    }
}
