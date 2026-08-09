package com.splitit.data.mapper

import com.splitit.data.database.Expense_participants
import com.splitit.data.database.Expenses
import com.splitit.data.database.Participants
import com.splitit.data.database.Sessions
import com.splitit.data.database.Settings
import com.splitit.data.database.Settlement_transfers
import com.splitit.data.database.Settlements
import com.splitit.domain.model.Expense
import com.splitit.domain.model.ExpenseParticipantShare
import com.splitit.domain.model.ExpenseSession
import com.splitit.domain.model.Participant
import com.splitit.domain.model.SessionStatus
import com.splitit.domain.model.Settlement
import com.splitit.domain.model.SettlementTransfer
import com.splitit.domain.repository.AppSettings
import com.splitit.domain.repository.ThemeMode
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SessionId
import com.splitit.domain.value.SettlementId
import com.splitit.domain.value.TransferId

fun Sessions.toDomain(
    participantIds: Set<ParticipantId> = emptySet(),
    expenseIds: Set<ExpenseId> = emptySet(),
): ExpenseSession {
    return ExpenseSession(
        id = SessionId(id),
        title = title,
        description = description,
        createdAtMillis = created_at,
        updatedAtMillis = updated_at,
        participantIds = participantIds,
        expenseIds = expenseIds,
        status = SessionStatus.valueOf(status),
    )
}

fun Participants.toDomain(): Participant {
    return Participant(
        id = ParticipantId(id),
        sessionId = SessionId(session_id),
        name = name,
        avatarColor = avatar_color,
        createdAtMillis = created_at,
        updatedAtMillis = updated_at,
    )
}

fun Expenses.toDomain(shares: List<ExpenseParticipantShare>): Expense {
    return Expense(
        id = ExpenseId(id),
        sessionId = SessionId(session_id),
        title = title,
        amount = Money(amount_minor, currency_code),
        payerId = ParticipantId(payer_participant_id),
        participantShares = shares,
        dateMillis = date_millis,
        note = note,
        createdAtMillis = created_at,
        updatedAtMillis = updated_at,
    )
}

fun Expense_participants.toDomain(): ExpenseParticipantShare {
    return ExpenseParticipantShare(
        expenseId = ExpenseId(expense_id),
        participantId = ParticipantId(participant_id),
        shareWeight = share_weight.toInt(),
    )
}

fun Settlements.toDomain(transfers: List<SettlementTransfer>): Settlement {
    return Settlement(
        id = SettlementId(id),
        sessionId = SessionId(session_id),
        generatedAtMillis = generated_at,
        sourceRevision = source_revision,
        transfers = transfers,
    )
}

fun Settlement_transfers.toDomain(): SettlementTransfer {
    return SettlementTransfer(
        id = TransferId(id),
        settlementId = SettlementId(settlement_id),
        fromParticipantId = ParticipantId(from_participant_id),
        toParticipantId = ParticipantId(to_participant_id),
        amount = Money(amount_minor, currency_code),
    )
}

fun Settings.toDomain(): AppSettings {
    return AppSettings(
        defaultCurrencyCode = default_currency_code,
        themeMode = ThemeMode.entries.firstOrNull { it.name == theme_mode } ?: ThemeMode.System,
    )
}
