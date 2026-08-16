package com.splitit.localization

import org.jetbrains.compose.resources.getString
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.error_add_participants_and_expense
import splitit.composeapp.generated.resources.error_choose_at_least_one_participant
import splitit.composeapp.generated.resources.error_choose_payer
import splitit.composeapp.generated.resources.error_could_not_delete_expense
import splitit.composeapp.generated.resources.error_could_not_delete_session
import splitit.composeapp.generated.resources.error_could_not_generate_settlement
import splitit.composeapp.generated.resources.error_could_not_load_expenses
import splitit.composeapp.generated.resources.error_could_not_load_participants
import splitit.composeapp.generated.resources.error_could_not_load_session
import splitit.composeapp.generated.resources.error_could_not_load_session_details
import splitit.composeapp.generated.resources.error_could_not_load_sessions
import splitit.composeapp.generated.resources.error_could_not_load_settings
import splitit.composeapp.generated.resources.error_could_not_load_settlement
import splitit.composeapp.generated.resources.error_could_not_remove_participant
import splitit.composeapp.generated.resources.error_could_not_save_expense
import splitit.composeapp.generated.resources.error_could_not_save_participant
import splitit.composeapp.generated.resources.error_could_not_save_session
import splitit.composeapp.generated.resources.error_could_not_save_settings
import splitit.composeapp.generated.resources.error_enter_expense_title
import splitit.composeapp.generated.resources.error_enter_participant_name
import splitit.composeapp.generated.resources.error_enter_positive_amount
import splitit.composeapp.generated.resources.error_enter_session_name
import splitit.composeapp.generated.resources.error_invalid_currency
import splitit.composeapp.generated.resources.error_participant_used_by_expenses

interface LocalizationService {
    fun getString(key: LocalizedString): String
}

class DefaultLocalizationService : LocalizationService {
    private val cache = mutableMapOf<LocalizedString, String>()
    private var initialized = false

    suspend fun initialize() {
        if (initialized) return
        LocalizedString.entries.forEach { key ->
            cache[key] = loadString(key)
        }
        initialized = true
    }

    override fun getString(key: LocalizedString): String {
        return cache[key] ?: key.key
    }

    private suspend fun loadString(key: LocalizedString): String {
        return try {
            when (key) {
                LocalizedString.ErrorCouldNotLoadSessions -> getString(Res.string.error_could_not_load_sessions)
                LocalizedString.ErrorCouldNotDeleteSession -> getString(Res.string.error_could_not_delete_session)
                LocalizedString.ErrorEnterSessionName -> getString(Res.string.error_enter_session_name)
                LocalizedString.ErrorCouldNotSaveSession -> getString(Res.string.error_could_not_save_session)
                LocalizedString.ErrorCouldNotLoadSession -> getString(Res.string.error_could_not_load_session)
                LocalizedString.ErrorCouldNotLoadSessionDetails -> getString(Res.string.error_could_not_load_session_details)
                LocalizedString.ErrorCouldNotLoadParticipants -> getString(Res.string.error_could_not_load_participants)
                LocalizedString.ErrorEnterParticipantName -> getString(Res.string.error_enter_participant_name)
                LocalizedString.ErrorCouldNotSaveParticipant -> getString(Res.string.error_could_not_save_participant)
                LocalizedString.ErrorParticipantUsedByExpenses -> getString(Res.string.error_participant_used_by_expenses)
                LocalizedString.ErrorCouldNotRemoveParticipant -> getString(Res.string.error_could_not_remove_participant)
                LocalizedString.ErrorCouldNotLoadExpenses -> getString(Res.string.error_could_not_load_expenses)
                LocalizedString.ErrorEnterExpenseTitle -> getString(Res.string.error_enter_expense_title)
                LocalizedString.ErrorEnterPositiveAmount -> getString(Res.string.error_enter_positive_amount)
                LocalizedString.ErrorChoosePayer -> getString(Res.string.error_choose_payer)
                LocalizedString.ErrorChooseAtLeastOneParticipant -> getString(Res.string.error_choose_at_least_one_participant)
                LocalizedString.ErrorCouldNotSaveExpense -> getString(Res.string.error_could_not_save_expense)
                LocalizedString.ErrorCouldNotDeleteExpense -> getString(Res.string.error_could_not_delete_expense)
                LocalizedString.ErrorCouldNotLoadSettlement -> getString(Res.string.error_could_not_load_settlement)
                LocalizedString.ErrorAddParticipantsAndExpense -> getString(Res.string.error_add_participants_and_expense)
                LocalizedString.ErrorCouldNotGenerateSettlement -> getString(Res.string.error_could_not_generate_settlement)
                LocalizedString.ErrorCouldNotLoadSettings -> getString(Res.string.error_could_not_load_settings)
                LocalizedString.ErrorInvalidCurrency -> getString(Res.string.error_invalid_currency)
                LocalizedString.ErrorCouldNotSaveSettings -> getString(Res.string.error_could_not_save_settings)
            }
        } catch (e: Exception) {
            key.key
        }
    }
}
