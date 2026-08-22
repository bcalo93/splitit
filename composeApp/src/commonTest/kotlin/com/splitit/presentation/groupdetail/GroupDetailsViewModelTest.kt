@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.groupdetail

import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.value.Money
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemoryGroupRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.group
import com.splitit.testutils.testLocalizationService
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class GroupDetailsViewModelTest {
    @Test
    fun loadsDetailsAndCanRefresh() = runViewModelTest {
        val groupRepository = InMemoryGroupRepository(listOf(group()))
        val participantRepository = InMemoryParticipantRepository(listOf(participant()))
        val viewModel = GroupDetailsViewModel(
            groupId = TestIds.group,
            observeGroupDetails = ObserveGroupDetailsUseCase(
                groupRepository,
                participantRepository,
                InMemoryExpenseRepository(),
                InMemorySettlementRepository(),
            ),
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(TestIds.group, viewModel.state.value.details?.group?.id)
        assertEquals(1, viewModel.state.value.details?.participants?.size)

        viewModel.refresh()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun aggregatesTotalSpentAcrossExpenses() = runViewModelTest {
        val groupRepository = InMemoryGroupRepository(listOf(group()))
        val participantRepository = InMemoryParticipantRepository(listOf(participant()))
        val expenseRepository = InMemoryExpenseRepository(
            listOf(
                expense(amount = Money(1_000L, "USD")),
                expense(id = TestIds.secondExpense, amount = Money(2_500L, "USD")),
            ),
        )
        val viewModel = GroupDetailsViewModel(
            groupId = TestIds.group,
            observeGroupDetails = ObserveGroupDetailsUseCase(
                groupRepository,
                participantRepository,
                expenseRepository,
                InMemorySettlementRepository(),
            ),
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertEquals(Money(3_500L, "USD"), viewModel.state.value.totalSpent)
    }

    @Test
    fun exposesNullTotalSpentWithoutExpenses() = runViewModelTest {
        val viewModel = GroupDetailsViewModel(
            groupId = TestIds.group,
            observeGroupDetails = ObserveGroupDetailsUseCase(
                InMemoryGroupRepository(listOf(group())),
                InMemoryParticipantRepository(listOf(participant())),
                InMemoryExpenseRepository(),
                InMemorySettlementRepository(),
            ),
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.totalSpent)
    }

    @Test
    fun exposesMissingGroupError() = runViewModelTest {
        val viewModel = GroupDetailsViewModel(
            groupId = TestIds.group,
            observeGroupDetails = ObserveGroupDetailsUseCase(
                InMemoryGroupRepository(),
                InMemoryParticipantRepository(),
                InMemoryExpenseRepository(),
                InMemorySettlementRepository(),
            ),
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Group group was not found.", viewModel.state.value.errorMessage)
    }
}
