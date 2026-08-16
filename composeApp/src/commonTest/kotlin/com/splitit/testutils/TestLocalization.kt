package com.splitit.testutils

import com.splitit.localization.LocalizationService

class TestLocalizationService : LocalizationService {
    override fun getString(key: String): String {
        return enStrings[key] ?: key
    }

    private val enStrings = mapOf(
        "error_could_not_load_sessions" to "Could not load sessions.",
        "error_could_not_delete_session" to "Could not delete the session.",
        "error_enter_session_name" to "Enter a session name.",
        "error_could_not_save_session" to "Could not save the session.",
        "error_could_not_load_session" to "Could not load the session.",
        "error_could_not_load_session_details" to "Could not load session details.",
        "error_could_not_load_participants" to "Could not load participants.",
        "error_enter_participant_name" to "Enter a participant name.",
        "error_could_not_save_participant" to "Could not save participant.",
        "error_participant_used_by_expenses" to "Participant cannot be removed because it is used by expenses.",
        "error_could_not_remove_participant" to "Could not remove participant.",
        "error_could_not_load_expenses" to "Could not load expenses.",
        "error_enter_expense_title" to "Enter an expense title.",
        "error_enter_positive_amount" to "Enter a positive amount.",
        "error_choose_payer" to "Choose who paid.",
        "error_choose_at_least_one_participant" to "Choose at least one participant.",
        "error_could_not_save_expense" to "Could not save expense.",
        "error_could_not_delete_expense" to "Could not delete expense.",
        "error_could_not_load_settlement" to "Could not load settlement.",
        "error_add_participants_and_expense" to "Add at least two participants and an expense first.",
        "error_could_not_generate_settlement" to "Could not generate settlement.",
        "error_could_not_load_settings" to "Could not load settings.",
        "error_invalid_currency" to "Use a 3-letter currency code, such as USD or EUR.",
        "error_could_not_save_settings" to "Could not save settings.",
    )
}

val testLocalizationService: LocalizationService = TestLocalizationService()
