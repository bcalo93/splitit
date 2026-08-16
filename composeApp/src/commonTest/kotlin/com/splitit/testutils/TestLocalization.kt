package com.splitit.testutils

import com.splitit.localization.LocalizedString
import com.splitit.localization.LocalizationService

class TestLocalizationService : LocalizationService {
    override fun getString(key: LocalizedString): String {
        return enStrings[key] ?: key.key
    }

    private val enStrings = mapOf(
        LocalizedString.ErrorCouldNotLoadSessions to "Could not load sessions.",
        LocalizedString.ErrorCouldNotDeleteSession to "Could not delete the session.",
        LocalizedString.ErrorEnterSessionName to "Enter a session name.",
        LocalizedString.ErrorCouldNotSaveSession to "Could not save the session.",
        LocalizedString.ErrorCouldNotLoadSession to "Could not load the session.",
        LocalizedString.ErrorCouldNotLoadSessionDetails to "Could not load session details.",
        LocalizedString.ErrorCouldNotLoadParticipants to "Could not load participants.",
        LocalizedString.ErrorEnterParticipantName to "Enter a participant name.",
        LocalizedString.ErrorCouldNotSaveParticipant to "Could not save participant.",
        LocalizedString.ErrorParticipantUsedByExpenses to "Participant cannot be removed because it is used by expenses.",
        LocalizedString.ErrorCouldNotRemoveParticipant to "Could not remove participant.",
        LocalizedString.ErrorCouldNotLoadExpenses to "Could not load expenses.",
        LocalizedString.ErrorEnterExpenseTitle to "Enter an expense title.",
        LocalizedString.ErrorEnterPositiveAmount to "Enter a positive amount.",
        LocalizedString.ErrorChoosePayer to "Choose who paid.",
        LocalizedString.ErrorChooseAtLeastOneParticipant to "Choose at least one participant.",
        LocalizedString.ErrorCouldNotSaveExpense to "Could not save expense.",
        LocalizedString.ErrorCouldNotDeleteExpense to "Could not delete expense.",
        LocalizedString.ErrorCouldNotLoadSettlement to "Could not load settlement.",
        LocalizedString.ErrorAddParticipantsAndExpense to "Add at least two participants and an expense first.",
        LocalizedString.ErrorCouldNotGenerateSettlement to "Could not generate settlement.",
        LocalizedString.ErrorCouldNotLoadSettings to "Could not load settings.",
        LocalizedString.ErrorInvalidCurrency to "Use a 3-letter currency code, such as USD or EUR.",
        LocalizedString.ErrorCouldNotSaveSettings to "Could not save settings.",
    )
}

val testLocalizationService: LocalizationService = TestLocalizationService()
