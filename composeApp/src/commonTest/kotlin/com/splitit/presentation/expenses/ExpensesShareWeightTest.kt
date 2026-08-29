@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.expenses

import com.splitit.domain.repository.AppSettings
import com.splitit.domain.usecase.CreateExpenseUseCase
import com.splitit.domain.usecase.DeleteExpenseUseCase
import com.splitit.domain.usecase.GetSettingsUseCase
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.usecase.UpdateExpenseUseCase
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemoryGroupRepository
import com.splitit.testutils.InMemorySettingsRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestClock
import com.splitit.testutils.TestIdGenerator
import com.splitit.testutils.TestIds
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.group
import com.splitit.testutils.testLocalizationService
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpensesShareAmountTest {
    @Test
    fun byAmountModePersistsAmountsOnSave() = runViewModelTest {
        val expenseRepository = InMemoryExpenseRepository()
        val viewModel = createViewModel(expenseRepository = expenseRepository)
        advanceUntilIdle()

        viewModel.onTitleChange("Dinner")
        viewModel.onAmountChange("10.00")
        viewModel.selectAllParticipants()
        viewModel.onSplitModeChanged(SplitMode.ByAmount)
        viewModel.onShareAmountChanged(TestIds.alice, "3.00")
        viewModel.onShareAmountChanged(TestIds.bob, "4.00")
        viewModel.onShareAmountChanged(TestIds.charlie, "3.00")
        viewModel.save()
        advanceUntilIdle()

        val saved = expenseRepository.savedExpenses.single()
        assertEquals(3, saved.participantShares.size)
        assertEquals(300L, saved.participantShares.first { it.participantId == TestIds.alice }.amountMinorUnits)
        assertEquals(400L, saved.participantShares.first { it.participantId == TestIds.bob }.amountMinorUnits)
        assertEquals(300L, saved.participantShares.first { it.participantId == TestIds.charlie }.amountMinorUnits)
    }

    @Test
    fun equalModePersistsEqualAmountsOnSave() = runViewModelTest {
        val expenseRepository = InMemoryExpenseRepository()
        val viewModel = createViewModel(expenseRepository = expenseRepository)
        advanceUntilIdle()

        viewModel.onTitleChange("Lunch")
        viewModel.onAmountChange("12.00")
        viewModel.onParticipantToggled(TestIds.bob)
        viewModel.save()
        advanceUntilIdle()

        val saved = expenseRepository.savedExpenses.single()
        assertEquals(2, saved.participantShares.size)
        assertTrue(saved.participantShares.all { it.amountMinorUnits == 600L })
    }

    @Test
    fun byAmountModeShowsErrorWhenSumExceedsTotal() = runViewModelTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAmountChange("10.00")
        viewModel.selectAllParticipants()
        viewModel.onSplitModeChanged(SplitMode.ByAmount)
        viewModel.onShareAmountChanged(TestIds.alice, "5.00")
        viewModel.onShareAmountChanged(TestIds.bob, "5.00")
        viewModel.onShareAmountChanged(TestIds.charlie, "1.00")

        val state = viewModel.state.value
        assertEquals(-100L, state.shareAmountsRemainder)
    }

    @Test
    fun byAmountModeShowsRemainder() = runViewModelTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAmountChange("10.00")
        viewModel.selectAllParticipants()
        viewModel.onSplitModeChanged(SplitMode.ByAmount)
        viewModel.onShareAmountChanged(TestIds.alice, "3.00")

        val state = viewModel.state.value
        assertEquals(700L, state.shareAmountsRemainder)
    }

    private fun createViewModel(
        expenseRepository: InMemoryExpenseRepository = InMemoryExpenseRepository(),
    ): ExpensesViewModel {
        val participantRepository = InMemoryParticipantRepository(
            listOf(
                participant(TestIds.alice),
                participant(TestIds.bob),
                participant(TestIds.charlie),
            ),
        )
        val groupRepository = InMemoryGroupRepository(listOf(group()))
        val settingsRepository = InMemorySettingsRepository(AppSettings(defaultCurrencyCode = "EUR"))
        val clock = TestClock(20L)
        return ExpensesViewModel(
            groupId = TestIds.group,
            observeGroupDetails = ObserveGroupDetailsUseCase(
                groupRepository,
                participantRepository,
                expenseRepository,
                InMemorySettlementRepository(),
            ),
            createExpense = CreateExpenseUseCase(
                groupRepository,
                participantRepository,
                expenseRepository,
                TestIdGenerator(),
                clock,
            ),
            updateExpense = UpdateExpenseUseCase(participantRepository, expenseRepository, clock),
            deleteExpense = DeleteExpenseUseCase(expenseRepository),
            clock = clock,
            getSettings = GetSettingsUseCase(settingsRepository),
            localization = testLocalizationService,
        )
    }
}
