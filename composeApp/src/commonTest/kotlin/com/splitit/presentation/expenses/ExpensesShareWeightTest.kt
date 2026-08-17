@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.expenses

import com.splitit.domain.repository.AppSettings
import com.splitit.domain.usecase.CreateExpenseUseCase
import com.splitit.domain.usecase.DeleteExpenseUseCase
import com.splitit.domain.usecase.GetSettingsUseCase
import com.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.splitit.domain.usecase.UpdateExpenseUseCase
import com.splitit.domain.value.Money
import com.splitit.testutils.InMemoryExpenseRepository
import com.splitit.testutils.InMemoryParticipantRepository
import com.splitit.testutils.InMemorySessionRepository
import com.splitit.testutils.InMemorySettingsRepository
import com.splitit.testutils.InMemorySettlementRepository
import com.splitit.testutils.TestClock
import com.splitit.testutils.TestIdGenerator
import com.splitit.testutils.TestIds
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.session
import com.splitit.testutils.testLocalizationService
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpensesShareWeightTest {
    @Test
    fun computeWeightedSharesSplitsProportionally() {
        val shares = computeWeightedShares(
            amountMinorUnits = 1_000L,
            weights = mapOf(TestIds.alice to 1, TestIds.bob to 3),
            currencyCode = "EUR",
        )

        assertEquals(Money(250L, "EUR"), shares[TestIds.alice])
        assertEquals(Money(750L, "EUR"), shares[TestIds.bob])
    }

    @Test
    fun computeWeightedSharesDistributesRemainderByLargestFraction() {
        val shares = computeWeightedShares(
            amountMinorUnits = 1_001L,
            weights = mapOf(
                TestIds.alice to 1,
                TestIds.bob to 1,
                TestIds.charlie to 1,
            ),
            currencyCode = "USD",
        )

        assertEquals(334L, shares.getValue(TestIds.alice).minorUnits)
        assertEquals(334L, shares.getValue(TestIds.bob).minorUnits)
        assertEquals(333L, shares.getValue(TestIds.charlie).minorUnits)
    }

    @Test
    fun weightedModeComputesTotalPartsAndLiveAmounts() = runViewModelTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAmountChange("9.00")
        viewModel.selectAllParticipants()
        viewModel.onSplitModeChanged(SplitMode.Weighted)
        viewModel.onShareWeightChanged(TestIds.bob, 2)

        assertEquals(4, viewModel.state.value.totalParts)
        assertEquals(
            Money(225L, "EUR"),
            viewModel.state.value.weightedShareAmounts?.get(TestIds.alice),
        )
        assertEquals(
            Money(450L, "EUR"),
            viewModel.state.value.weightedShareAmounts?.get(TestIds.bob),
        )
        assertEquals(
            Money(225L, "EUR"),
            viewModel.state.value.weightedShareAmounts?.get(TestIds.charlie),
        )
    }

    @Test
    fun weightedModePersistsShareWeightsOnSave() = runViewModelTest {
        val expenseRepository = InMemoryExpenseRepository()
        val viewModel = createViewModel(expenseRepository = expenseRepository)
        advanceUntilIdle()

        viewModel.onTitleChange("Dinner")
        viewModel.onAmountChange("10.00")
        viewModel.selectAllParticipants()
        viewModel.onSplitModeChanged(SplitMode.Weighted)
        viewModel.onShareWeightChanged(TestIds.bob, 3)
        viewModel.save()
        advanceUntilIdle()

        val saved = expenseRepository.savedExpenses.single()
        assertEquals(3, saved.participantShares.size)
        assertEquals(1, saved.participantShares.first { it.participantId == TestIds.alice }.shareWeight)
        assertEquals(3, saved.participantShares.first { it.participantId == TestIds.bob }.shareWeight)
        assertEquals(1, saved.participantShares.first { it.participantId == TestIds.charlie }.shareWeight)
    }

    @Test
    fun equalModePersistsDefaultWeights() = runViewModelTest {
        val expenseRepository = InMemoryExpenseRepository()
        val viewModel = createViewModel(expenseRepository = expenseRepository)
        advanceUntilIdle()

        viewModel.onTitleChange("Lunch")
        viewModel.onAmountChange("12.00")
        viewModel.onParticipantToggled(TestIds.bob)
        viewModel.save()
        advanceUntilIdle()

        val saved = expenseRepository.savedExpenses.single()
        assert(saved.participantShares.all { it.shareWeight == 1 })
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
        val sessionRepository = InMemorySessionRepository(listOf(session()))
        val settingsRepository = InMemorySettingsRepository(AppSettings(defaultCurrencyCode = "EUR"))
        val clock = TestClock(20L)
        return ExpensesViewModel(
            sessionId = TestIds.session,
            observeSessionDetails = ObserveSessionDetailsUseCase(
                sessionRepository,
                participantRepository,
                expenseRepository,
                InMemorySettlementRepository(),
            ),
            createExpense = CreateExpenseUseCase(
                sessionRepository,
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
