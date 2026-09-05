@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.settlement

import com.splitit.domain.model.Debt
import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.ExpenseType
import com.splitit.domain.model.Participant
import com.splitit.domain.model.Settlement
import com.splitit.domain.service.SourceRevisionCalculator
import com.splitit.domain.usecase.CalculateGroupBalancesParams
import com.splitit.domain.usecase.CalculateGroupBalancesUseCase
import com.splitit.domain.usecase.GenerateSettlementParams
import com.splitit.domain.usecase.GenerateSettlementUseCase
import com.splitit.domain.usecase.GroupDetails
import com.splitit.domain.usecase.ObserveGroupDetailsParams
import com.splitit.domain.usecase.RecordTransferPaymentParams
import com.splitit.domain.usecase.RecordTransferPaymentUseCase
import com.splitit.domain.usecase.UseCase
import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import com.splitit.testutils.group
import com.splitit.testutils.participant
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.settlement
import com.splitit.testutils.testLocalizationService
import com.splitit.testutils.transfer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SettlementViewModelTest {
    @Test
    fun calculatesSettlementOnLoadAndRefreshesItAfterSourceChanges() = runViewModelTest {
        val participants = listOf(participant(TestIds.alice), participant(TestIds.bob))
        val initialExpense = expense()
        val initialRevision = SourceRevisionCalculator.calculate(participants, listOf(initialExpense))
        val initialDetails = groupDetails(participants = participants, expenses = listOf(initialExpense))
        val settledDetails = initialDetails.copy(
            latestSettlement = settlement(transfers = listOf(transfer()), sourceRevision = initialRevision),
        )
        val editedExpense = initialExpense.copy(title = "Updated dinner", updatedAtMillis = 2L)
        val editedRevision = SourceRevisionCalculator.calculate(participants, listOf(editedExpense))
        val detailsAfterEdit = initialDetails.copy(
            expenses = listOf(editedExpense),
            latestSettlement = settlement(transfers = listOf(transfer()), sourceRevision = initialRevision),
        )
        val settledAfterEditDetails = detailsAfterEdit.copy(
            latestSettlement = settlement(transfers = listOf(transfer()), sourceRevision = editedRevision),
        )

        val observeGroupDetails = mockk<UseCase<ObserveGroupDetailsParams, GroupDetails>>()
        coEvery { observeGroupDetails(any()) } returnsMany listOf(
            initialDetails,
            settledDetails,
            detailsAfterEdit,
            settledAfterEditDetails,
        )

        val generateSettlement = mockk<GenerateSettlementUseCase>()
        coEvery { generateSettlement(any()) } returns settlement(transfers = listOf(transfer()))
        val calculateGroupBalances = mockk<CalculateGroupBalancesUseCase>()
        coEvery { calculateGroupBalances(any()) } returns emptyList()
        val recordTransferPayment = mockk<RecordTransferPaymentUseCase>()

        val viewModel = SettlementViewModel(
            groupId = TestIds.group,
            observeGroupDetails = observeGroupDetails,
            calculateGroupBalances = calculateGroupBalances,
            generateSettlement = generateSettlement,
            recordTransferPaymentUseCase = recordTransferPayment,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.canGenerateSettlement)
        assertNotNull(viewModel.state.value.settlement)
        assertFalse(viewModel.state.value.isSettlementStale)

        viewModel.refresh()
        advanceUntilIdle()

        coVerify(exactly = 4) { observeGroupDetails(ObserveGroupDetailsParams(TestIds.group)) }
        coVerify(exactly = 2) { generateSettlement(GenerateSettlementParams(TestIds.group)) }
        coVerify(exactly = 2) { calculateGroupBalances(CalculateGroupBalancesParams(TestIds.group)) }
    }

    @Test
    fun recordsTransferPaymentAndRegeneratesSettlement() = runViewModelTest {
        val participants = listOf(participant(TestIds.alice), participant(TestIds.bob))
        val initialExpense = expense()
        val initialRevision = SourceRevisionCalculator.calculate(participants, listOf(initialExpense))
        val initialDetails = groupDetails(participants = participants, expenses = listOf(initialExpense))
        val transferInstance = transfer()
        val settlementWithTransfer = settlement(transfers = listOf(transferInstance), sourceRevision = initialRevision)
        val settledDetails = initialDetails.copy(latestSettlement = settlementWithTransfer)
        val emptySettlement = settlement(transfers = emptyList(), sourceRevision = initialRevision)
        val detailsAfterPayment = initialDetails.copy(latestSettlement = emptySettlement)

        val observeGroupDetails = mockk<UseCase<ObserveGroupDetailsParams, GroupDetails>>()
        coEvery { observeGroupDetails(any()) } returnsMany listOf(
            initialDetails,
            settledDetails,
            detailsAfterPayment,
        )

        val generateSettlement = mockk<GenerateSettlementUseCase>()
        coEvery { generateSettlement(any()) } returns settlementWithTransfer
        val calculateGroupBalances = mockk<CalculateGroupBalancesUseCase>()
        coEvery { calculateGroupBalances(any()) } returns emptyList()
        val recordTransferPayment = mockk<RecordTransferPaymentUseCase>()
        coEvery { recordTransferPayment(any()) } returns expense(type = ExpenseType.TRANSFER_PAYMENT)

        val viewModel = SettlementViewModel(
            groupId = TestIds.group,
            observeGroupDetails = observeGroupDetails,
            calculateGroupBalances = calculateGroupBalances,
            generateSettlement = generateSettlement,
            recordTransferPaymentUseCase = recordTransferPayment,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        val recordedTransfer = viewModel.state.value.settlement?.transfers?.first()
        assertNotNull(recordedTransfer)

        viewModel.recordTransferPayment(recordedTransfer)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            recordTransferPayment(
                RecordTransferPaymentParams(
                    groupId = TestIds.group,
                    debt = Debt(
                        fromParticipantId = recordedTransfer.fromParticipantId,
                        toParticipantId = recordedTransfer.toParticipantId,
                        amount = recordedTransfer.amount,
                    ),
                ),
            )
        }
    }

    @Test
    fun blocksGenerationWhenThereAreNotEnoughParticipantsOrExpenses() = runViewModelTest {
        val detailsWithoutEnoughData = groupDetails(
            participants = listOf(participant()),
            expenses = emptyList(),
        )
        val observeGroupDetails = mockk<UseCase<ObserveGroupDetailsParams, GroupDetails>>()
        coEvery { observeGroupDetails(any()) } returns detailsWithoutEnoughData

        val calculateGroupBalances = mockk<CalculateGroupBalancesUseCase>()
        coEvery { calculateGroupBalances(any()) } returns emptyList()
        val generateSettlement = mockk<GenerateSettlementUseCase>()
        val recordTransferPayment = mockk<RecordTransferPaymentUseCase>()

        val viewModel = SettlementViewModel(
            groupId = TestIds.group,
            observeGroupDetails = observeGroupDetails,
            calculateGroupBalances = calculateGroupBalances,
            generateSettlement = generateSettlement,
            recordTransferPaymentUseCase = recordTransferPayment,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.canGenerateSettlement)
        viewModel.generate()

        assertEquals(
            "Add at least two participants and an expense first.",
            viewModel.state.value.errorMessage,
        )
        coVerify(exactly = 0) { generateSettlement(any()) }
    }

    private fun groupDetails(
        group: ExpenseGroup = group(),
        participants: List<Participant>,
        expenses: List<Expense>,
        latestSettlement: Settlement? = null,
    ): GroupDetails = GroupDetails(
        group = group,
        participants = participants,
        expenses = expenses,
        latestSettlement = latestSettlement,
    )
}
