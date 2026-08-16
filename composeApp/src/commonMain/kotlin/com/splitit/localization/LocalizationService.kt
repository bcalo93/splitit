package com.splitit.localization

enum class LocalizationKey {
    ErrorCouldNotLoadSessions,
    ErrorCouldNotDeleteSession,
    ErrorEnterSessionName,
    ErrorCouldNotSaveSession,
    ErrorCouldNotLoadSession,
    ErrorCouldNotLoadSessionDetails,
    ErrorCouldNotLoadParticipants,
    ErrorEnterParticipantName,
    ErrorCouldNotSaveParticipant,
    ErrorParticipantUsedByExpenses,
    ErrorCouldNotRemoveParticipant,
    ErrorCouldNotLoadExpenses,
    ErrorEnterExpenseTitle,
    ErrorEnterPositiveAmount,
    ErrorChoosePayer,
    ErrorChooseAtLeastOneParticipant,
    ErrorCouldNotSaveExpense,
    ErrorCouldNotDeleteExpense,
    ErrorCouldNotLoadSettlement,
    ErrorAddParticipantsAndExpense,
    ErrorCouldNotGenerateSettlement,
    ErrorCouldNotLoadSettings,
    ErrorInvalidCurrency,
    ErrorCouldNotSaveSettings,
}

interface LocalizationService {
    fun getString(key: LocalizationKey): String
}

class DefaultLocalizationService(private val deviceLocale: DeviceLocale) : LocalizationService {
    override fun getString(key: LocalizationKey): String {
        val locale = deviceLocale.getLanguage()
        val strings = localizedStrings[locale] ?: localizedStrings["en"]!!
        return strings[key] ?: key.name
    }

    private val localizedStrings = mapOf(
        "en" to mapOf(
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
        ),
        "es" to mapOf(
            LocalizationKey.ErrorCouldNotLoadSessions to "No se pudieron cargar las sesiones.",
            LocalizationKey.ErrorCouldNotDeleteSession to "No se pudo eliminar la sesión.",
            LocalizationKey.ErrorEnterSessionName to "Ingresa un nombre para la sesión.",
            LocalizationKey.ErrorCouldNotSaveSession to "No se pudo guardar la sesión.",
            LocalizationKey.ErrorCouldNotLoadSession to "No se pudo cargar la sesión.",
            LocalizationKey.ErrorCouldNotLoadSessionDetails to "No se pudieron cargar los detalles de la sesión.",
            LocalizationKey.ErrorCouldNotLoadParticipants to "No se pudieron cargar los participantes.",
            LocalizationKey.ErrorEnterParticipantName to "Ingresa un nombre de participante.",
            LocalizationKey.ErrorCouldNotSaveParticipant to "No se pudo guardar el participante.",
            LocalizationKey.ErrorParticipantUsedByExpenses to "El participante no puede ser eliminado porque está siendo usado por gastos.",
            LocalizationKey.ErrorCouldNotRemoveParticipant to "No se pudo eliminar el participante.",
            LocalizationKey.ErrorCouldNotLoadExpenses to "No se pudieron cargar los gastos.",
            LocalizationKey.ErrorEnterExpenseTitle to "Ingresa un título para el gasto.",
            LocalizationKey.ErrorEnterPositiveAmount to "Ingresa un monto positivo.",
            LocalizationKey.ErrorChoosePayer to "Elige quién pagó.",
            LocalizationKey.ErrorChooseAtLeastOneParticipant to "Elige al menos un participante.",
            LocalizationKey.ErrorCouldNotSaveExpense to "No se pudo guardar el gasto.",
            LocalizationKey.ErrorCouldNotDeleteExpense to "No se pudo eliminar el gasto.",
            LocalizationKey.ErrorCouldNotLoadSettlement to "No se pudo cargar la liquidación.",
            LocalizationKey.ErrorAddParticipantsAndExpense to "Agrega al menos dos participantes y un gasto primero.",
            LocalizationKey.ErrorCouldNotGenerateSettlement to "No se pudo generar la liquidación.",
            LocalizationKey.ErrorCouldNotLoadSettings to "No se pudo cargar la configuración.",
            LocalizationKey.ErrorInvalidCurrency to "Usa un código de moneda de 3 letras, como USD o EUR.",
            LocalizationKey.ErrorCouldNotSaveSettings to "No se pudo guardar la configuración.",
        ),
    )
}
