package com.splitit.testutils

import com.splitit.localization.LocalizationKey
import com.splitit.localization.LocalizationService

class TestLocalizationService : LocalizationService {
    override fun getString(key: LocalizationKey): String {
        return enStrings[key] ?: key.name
    }

    private val enStrings = mapOf(
        LocalizationKey.ErrorCouldNotLoadSessions to "Could not load sessions.",
        LocalizationKey.ErrorCouldNotDeleteSession to "Could not delete the session.",
        LocalizationKey.ErrorEnterSessionName to "Enter a session name.",
        LocalizationKey.ErrorCouldNotSaveSession to "Could not save the session.",
        LocalizationKey.ErrorCouldNotLoadSession to "Could not load the session.",
        LocalizationKey.ErrorCouldNotLoadSessionDetails to "Could not load session details.",
        LocalizationKey.ErrorCouldNotLoadParticipants to "Could not load participants.",
        LocalizationKey.ErrorEnterParticipantName to "Enter a participant name.",
        LocalizationKey.ErrorCouldNotSaveParticipant to "Could not save participant.",
        LocalizationKey.ErrorParticipantUsedByExpenses to "Participant cannot be removed because it is used by expenses.",
        LocalizationKey.ErrorCouldNotRemoveParticipant to "Could not remove participant.",
        LocalizationKey.ErrorCouldNotLoadExpenses to "Could not load expenses.",
        LocalizationKey.ErrorEnterExpenseTitle to "Enter an expense title.",
        LocalizationKey.ErrorEnterPositiveAmount to "Enter a positive amount.",
        LocalizationKey.ErrorChoosePayer to "Choose who paid.",
        LocalizationKey.ErrorChooseAtLeastOneParticipant to "Choose at least one participant.",
        LocalizationKey.ErrorCouldNotSaveExpense to "Could not save expense.",
        LocalizationKey.ErrorCouldNotDeleteExpense to "Could not delete expense.",
        LocalizationKey.ErrorCouldNotLoadSettlement to "Could not load settlement.",
        LocalizationKey.ErrorAddParticipantsAndExpense to "Add at least two participants and an expense first.",
        LocalizationKey.ErrorCouldNotGenerateSettlement to "Could not generate settlement.",
        LocalizationKey.ErrorCouldNotLoadSettings to "Could not load settings.",
        LocalizationKey.ErrorInvalidCurrency to "Use a 3-letter currency code, such as USD or EUR.",
        LocalizationKey.ErrorCouldNotSaveSettings to "Could not save settings.",
    )
}

val testLocalizationService: LocalizationService = TestLocalizationService()
