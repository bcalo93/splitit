package com.splitit.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.splitit.domain.value.SessionId
import com.splitit.presentation.settings.SettingsViewModel
import com.splitit.routes.sessions.SessionDetailsRoute
import com.splitit.routes.sessions.SessionFormRoute
import com.splitit.routes.sessions.SessionsRoute
import com.splitit.routes.sessions.expenses.ExpensesRoute
import com.splitit.routes.sessions.participants.ParticipantsRoute
import com.splitit.routes.sessions.settlement.SettlementRoute
import com.splitit.routes.settings.SettingsRoute

private const val DELIMITER = "|"

private const val ROUTE_SESSIONS = "sessions"
private const val ROUTE_DETAILS = "details"
private const val ROUTE_FORM = "form"
private const val ROUTE_PARTICIPANTS = "participants"
private const val ROUTE_EXPENSES = "expenses"
private const val ROUTE_SETTLEMENT = "settlement"
private const val ROUTE_SETTINGS = "settings"

sealed class Route(val key: String) {
    @Composable
    abstract fun Content(
        settingsViewModel: SettingsViewModel,
        onNavigate: (Route) -> Unit,
        nextFormKey: () -> Int,
    )

    data object Sessions : Route(ROUTE_SESSIONS) {
        @Composable
        override fun Content(
            settingsViewModel: SettingsViewModel,
            onNavigate: (Route) -> Unit,
            nextFormKey: () -> Int,
        ) {
            SessionsRoute(
                onCreate = { onNavigate(Form(null, nextFormKey())) },
                onOpen = { sessionId -> onNavigate(Details(sessionId)) },
                onEdit = { sessionId -> onNavigate(Form(sessionId, nextFormKey())) },
                onSettings = { onNavigate(Settings) },
            )
        }
    }

    data class Details(val sessionId: SessionId) : Route(ROUTE_DETAILS) {
        @Composable
        override fun Content(
            settingsViewModel: SettingsViewModel,
            onNavigate: (Route) -> Unit,
            nextFormKey: () -> Int,
        ) {
            SessionDetailsRoute(
                sessionId = sessionId,
                onBack = { onNavigate(Sessions) },
                onEdit = { onNavigate(Form(sessionId, nextFormKey())) },
                onParticipants = { onNavigate(Participants(sessionId)) },
                onExpenses = { onNavigate(Expenses(sessionId)) },
                onSettlement = { onNavigate(Settlement(sessionId)) },
            )
        }
    }

    data class Form(val sessionId: SessionId?, val formKey: Int) : Route(ROUTE_FORM) {
        @Composable
        override fun Content(
            settingsViewModel: SettingsViewModel,
            onNavigate: (Route) -> Unit,
            nextFormKey: () -> Int,
        ) {
            SessionFormRoute(
                sessionId = sessionId,
                formKey = formKey,
                onBack = { onNavigate(Sessions) },
                onSaved = { savedSessionId -> onNavigate(Details(savedSessionId)) },
            )
        }
    }

    data class Participants(val sessionId: SessionId) : Route(ROUTE_PARTICIPANTS) {
        @Composable
        override fun Content(
            settingsViewModel: SettingsViewModel,
            onNavigate: (Route) -> Unit,
            nextFormKey: () -> Int,
        ) {
            ParticipantsRoute(
                sessionId = sessionId,
                onBack = { onNavigate(Details(sessionId)) },
            )
        }
    }

    data class Expenses(val sessionId: SessionId) : Route(ROUTE_EXPENSES) {
        @Composable
        override fun Content(
            settingsViewModel: SettingsViewModel,
            onNavigate: (Route) -> Unit,
            nextFormKey: () -> Int,
        ) {
            ExpensesRoute(
                sessionId = sessionId,
                onBack = { onNavigate(Details(sessionId)) },
            )
        }
    }

    data class Settlement(val sessionId: SessionId) : Route(ROUTE_SETTLEMENT) {
        @Composable
        override fun Content(
            settingsViewModel: SettingsViewModel,
            onNavigate: (Route) -> Unit,
            nextFormKey: () -> Int,
        ) {
            SettlementRoute(
                sessionId = sessionId,
                onBack = { onNavigate(Details(sessionId)) },
            )
        }
    }

    data object Settings : Route(ROUTE_SETTINGS) {
        @Composable
        override fun Content(
            settingsViewModel: SettingsViewModel,
            onNavigate: (Route) -> Unit,
            nextFormKey: () -> Int,
        ) {
            SettingsRoute(
                onBack = { onNavigate(Sessions) },
                viewModel = settingsViewModel,
            )
        }
    }
}

private fun routeSaver(): Saver<Route, String> = Saver(
    save = { route ->
        when (route) {
            is Route.Sessions -> route.key
            is Route.Details -> "${route.key}${DELIMITER}${route.sessionId.value}"
            is Route.Form -> "${route.key}${DELIMITER}${route.sessionId?.value ?: ""}${DELIMITER}${route.formKey}"
            is Route.Participants -> "${route.key}${DELIMITER}${route.sessionId.value}"
            is Route.Expenses -> "${route.key}${DELIMITER}${route.sessionId.value}"
            is Route.Settlement -> "${route.key}${DELIMITER}${route.sessionId.value}"
            is Route.Settings -> route.key
        }
    },
    restore = { value ->
        val parts = value.split(DELIMITER)
        when (parts.firstOrNull()) {
            ROUTE_SESSIONS -> Route.Sessions
            ROUTE_DETAILS -> Route.Details(SessionId(parts[1]))
            ROUTE_FORM -> Route.Form(
                sessionId = parts[1].takeIf { it.isNotBlank() }?.let(::SessionId),
                formKey = parts.getOrNull(2)?.toIntOrNull() ?: 0,
            )
            ROUTE_PARTICIPANTS -> Route.Participants(SessionId(parts[1]))
            ROUTE_EXPENSES -> Route.Expenses(SessionId(parts[1]))
            ROUTE_SETTLEMENT -> Route.Settlement(SessionId(parts[1]))
            ROUTE_SETTINGS -> Route.Settings
            else -> Route.Sessions
        }
    },
)

@Composable
fun SplitItRoutes(
    settingsViewModel: SettingsViewModel,
) {
    var currentRoute by rememberSaveable(stateSaver = routeSaver()) { mutableStateOf<Route>(Route.Sessions) }
    var formKeyCounter by rememberSaveable { mutableIntStateOf(0) }

    currentRoute.Content(
        settingsViewModel = settingsViewModel,
        onNavigate = { route -> currentRoute = route },
        nextFormKey = { ++formKeyCounter },
    )
}
