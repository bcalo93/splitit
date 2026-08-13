package com.splitit.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

private const val ROUTE_SESSIONS = "sessions"
private const val ROUTE_DETAILS = "details"
private const val ROUTE_FORM = "form"
private const val ROUTE_PARTICIPANTS = "participants"
private const val ROUTE_EXPENSES = "expenses"
private const val ROUTE_SETTLEMENT = "settlement"
private const val ROUTE_SETTINGS = "settings"

@Composable
fun SplitItRoutes(
    settingsViewModel: SettingsViewModel,
) {
    var route by rememberSaveable { mutableStateOf(ROUTE_SESSIONS) }
    var routeSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    var routeFormKey by rememberSaveable { mutableStateOf(0) }

    when (route) {
        ROUTE_DETAILS -> {
            val sessionId = routeSessionId
            if (sessionId == null) {
                route = ROUTE_SESSIONS
            } else {
                SessionDetailsRoute(
                    sessionId = SessionId(sessionId),
                    onBack = { route = ROUTE_SESSIONS },
                    onEdit = {
                        routeSessionId = sessionId
                        routeFormKey += 1
                        route = ROUTE_FORM
                    },
                    onParticipants = {
                        routeSessionId = sessionId
                        route = ROUTE_PARTICIPANTS
                    },
                    onExpenses = {
                        routeSessionId = sessionId
                        route = ROUTE_EXPENSES
                    },
                    onSettlement = {
                        routeSessionId = sessionId
                        route = ROUTE_SETTLEMENT
                    },
                )
            }
        }

        ROUTE_EXPENSES -> {
            val sessionId = routeSessionId
            if (sessionId == null) {
                route = ROUTE_SESSIONS
            } else {
                ExpensesRoute(
                    sessionId = SessionId(sessionId),
                    onBack = { route = ROUTE_DETAILS },
                )
            }
        }

        ROUTE_PARTICIPANTS -> {
            val sessionId = routeSessionId
            if (sessionId == null) {
                route = ROUTE_SESSIONS
            } else {
                ParticipantsRoute(
                    sessionId = SessionId(sessionId),
                    onBack = { route = ROUTE_DETAILS },
                )
            }
        }

        ROUTE_SETTLEMENT -> {
            val sessionId = routeSessionId
            if (sessionId == null) {
                route = ROUTE_SESSIONS
            } else {
                SettlementRoute(
                    sessionId = SessionId(sessionId),
                    onBack = { route = ROUTE_DETAILS },
                )
            }
        }

        ROUTE_SETTINGS -> {
            SettingsRoute(
                onBack = { route = ROUTE_SESSIONS },
                viewModel = settingsViewModel,
            )
        }

        ROUTE_FORM -> {
            SessionFormRoute(
                sessionId = routeSessionId?.let(::SessionId),
                formKey = routeFormKey,
                onBack = { route = ROUTE_SESSIONS },
                onSaved = { savedSessionId ->
                    routeSessionId = savedSessionId.value
                    route = ROUTE_DETAILS
                },
            )
        }

        else -> {
            SessionsRoute(
                onCreate = {
                    routeSessionId = null
                    routeFormKey += 1
                    route = ROUTE_FORM
                },
                onOpen = { sessionId ->
                    routeSessionId = sessionId.value
                    route = ROUTE_DETAILS
                },
                onEdit = { sessionId ->
                    routeSessionId = sessionId.value
                    routeFormKey += 1
                    route = ROUTE_FORM
                },
                onSettings = { route = ROUTE_SETTINGS },
            )
        }
    }
}
