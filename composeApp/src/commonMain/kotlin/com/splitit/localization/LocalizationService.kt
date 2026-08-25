package com.splitit.localization

import org.jetbrains.compose.resources.getString
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.error_add_participants_and_expense
import splitit.composeapp.generated.resources.error_choose_at_least_one_participant
import splitit.composeapp.generated.resources.error_choose_payer
import splitit.composeapp.generated.resources.error_could_not_delete_expense
import splitit.composeapp.generated.resources.error_could_not_delete_group
import splitit.composeapp.generated.resources.error_could_not_generate_settlement
import splitit.composeapp.generated.resources.error_could_not_load_expenses
import splitit.composeapp.generated.resources.error_could_not_load_participants
import splitit.composeapp.generated.resources.error_could_not_load_group
import splitit.composeapp.generated.resources.error_could_not_load_group_details
import splitit.composeapp.generated.resources.error_could_not_load_groups
import splitit.composeapp.generated.resources.error_could_not_load_settings
import splitit.composeapp.generated.resources.error_could_not_load_settlement
import splitit.composeapp.generated.resources.error_could_not_remove_participant
import splitit.composeapp.generated.resources.error_could_not_save_expense
import splitit.composeapp.generated.resources.error_could_not_save_participant
import splitit.composeapp.generated.resources.error_could_not_save_group
import splitit.composeapp.generated.resources.error_could_not_save_settings
import splitit.composeapp.generated.resources.error_could_not_record_payment
import splitit.composeapp.generated.resources.error_enter_expense_title
import splitit.composeapp.generated.resources.error_enter_participant_name
import splitit.composeapp.generated.resources.error_enter_positive_amount
import splitit.composeapp.generated.resources.error_enter_group_name
import splitit.composeapp.generated.resources.error_invalid_currency
import splitit.composeapp.generated.resources.error_participant_used_by_expenses
import splitit.composeapp.generated.resources.payment_title

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
                LocalizedString.ErrorCouldNotLoadGroups -> getString(Res.string.error_could_not_load_groups)
                LocalizedString.ErrorCouldNotDeleteGroup -> getString(Res.string.error_could_not_delete_group)
                LocalizedString.ErrorEnterGroupName -> getString(Res.string.error_enter_group_name)
                LocalizedString.ErrorCouldNotSaveGroup -> getString(Res.string.error_could_not_save_group)
                LocalizedString.ErrorCouldNotLoadGroup -> getString(Res.string.error_could_not_load_group)
                LocalizedString.ErrorCouldNotLoadGroupDetails -> getString(Res.string.error_could_not_load_group_details)
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
                LocalizedString.PaymentTitle -> getString(Res.string.payment_title)
                LocalizedString.ErrorCouldNotRecordPayment -> getString(Res.string.error_could_not_record_payment)
            }
        } catch (e: Exception) {
            key.key
        }
    }
}
