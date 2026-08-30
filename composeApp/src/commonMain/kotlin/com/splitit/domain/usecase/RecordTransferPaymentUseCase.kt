package com.splitit.domain.usecase

import com.splitit.domain.model.Debt
import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseParticipantShare
import com.splitit.domain.model.ExpenseType
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.value.Clock
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.IdGenerator
import com.splitit.localization.LocalizedString
import com.splitit.localization.LocalizationService

class RecordTransferPaymentUseCase(
    private val groupRepository: GroupRepository,
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: ExpenseRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
    private val localization: LocalizationService,
) {
    suspend operator fun invoke(
        groupId: GroupId,
        debt: Debt,
    ): Expense {
        requireNotNull(groupRepository.getGroup(groupId)) {
            "Group ${groupId.value} was not found."
        }
        require(debt.fromParticipantId != debt.toParticipantId) {
            "Transfer payment endpoints must be different."
        }

        val groupParticipants = participantRepository.getParticipants(groupId)
        val participantById = groupParticipants.associateBy { it.id }
        val fromParticipant = requireNotNull(participantById[debt.fromParticipantId]) {
            "Payer ${debt.fromParticipantId.value} does not belong to the group."
        }
        val toParticipant = requireNotNull(participantById[debt.toParticipantId]) {
            "Recipient ${debt.toParticipantId.value} does not belong to the group."
        }

        val now = clock.nowMillis()
        val expenseId = idGenerator.newExpenseId()
        val titlePattern = localization.getString(LocalizedString.PaymentTitle)
        val title = titlePattern
            .replace("%1\$s", fromParticipant.name)
            .replace("%2\$s", toParticipant.name)

        val expense = Expense(
            id = expenseId,
            groupId = groupId,
            title = title,
            amount = debt.amount,
            payerId = debt.fromParticipantId,
            participantShares = listOf(
                ExpenseParticipantShare(
                    expenseId = expenseId,
                    participantId = debt.toParticipantId,
                    amountMinorUnits = debt.amount.minorUnits,
                ),
            ),
            dateMillis = now,
            note = null,
            createdAtMillis = now,
            updatedAtMillis = now,
            type = ExpenseType.TRANSFER_PAYMENT,
        )

        expenseRepository.saveExpense(expense)
        return expense
    }
}
