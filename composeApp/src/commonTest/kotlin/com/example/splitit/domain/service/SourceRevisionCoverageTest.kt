package com.example.splitit.domain.service

import com.example.splitit.domain.value.Money
import com.example.splitit.testutils.TestIds
import com.example.splitit.testutils.expense
import com.example.splitit.testutils.participant
import kotlin.test.Test
import kotlin.test.assertNotEquals

class SourceRevisionCoverageTest {
    @Test
    fun changesWhenAnySettlementSourceFieldChanges() {
        val baseParticipant = participant(
            id = TestIds.alice,
            name = "Alice",
            avatarColor = "#000000",
        )
        val baseExpense = expense(
            amount = Money(1_000L, "USD"),
            payerId = TestIds.alice,
            participantIds = listOf(TestIds.alice, TestIds.bob),
            note = "Dinner",
        )
        val participants = listOf(baseParticipant, participant(TestIds.bob))
        val baseline = SourceRevisionCalculator.calculate(participants, listOf(baseExpense))

        val changedSources = listOf(
            participants.map { if (it.id == TestIds.alice) it.copy(name = "Renamed") else it },
            participants.map { if (it.id == TestIds.alice) it.copy(avatarColor = "#FFFFFF") else it },
            participants.map { if (it.id == TestIds.alice) it.copy(updatedAtMillis = 2L) else it },
        ) to listOf(
            baseExpense.copy(amount = Money(1_001L, "USD")),
            baseExpense.copy(amount = Money(1_000L, "EUR")),
            baseExpense.copy(payerId = TestIds.bob),
            baseExpense.copy(dateMillis = 2L),
            baseExpense.copy(note = "Lunch"),
            baseExpense.copy(createdAtMillis = 2L, updatedAtMillis = 2L),
            baseExpense.copy(updatedAtMillis = 2L),
            baseExpense.copy(
                participantShares = baseExpense.participantShares.map {
                    if (it.participantId == TestIds.alice) it.copy(shareWeight = 2) else it
                },
            ),
        )

        changedSources.first.forEach { changedParticipants ->
            assertNotEquals(
                baseline,
                SourceRevisionCalculator.calculate(changedParticipants, listOf(baseExpense)),
            )
        }
        changedSources.second.forEach { changedExpense ->
            assertNotEquals(
                baseline,
                SourceRevisionCalculator.calculate(participants, listOf(changedExpense)),
            )
        }
    }
}
