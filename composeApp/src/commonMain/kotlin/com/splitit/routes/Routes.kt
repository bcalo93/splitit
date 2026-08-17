package com.splitit.routes

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.splitit.domain.value.SessionId
import com.splitit.presentation.settings.SettingsViewModel
import com.splitit.routes.sessions.SessionDetailsRoute
import com.splitit.routes.sessions.SessionFormRoute
import com.splitit.routes.sessions.SessionsRoute
import com.splitit.routes.sessions.expenses.ExpensesRoute
import com.splitit.routes.sessions.participants.ParticipantsRoute
import com.splitit.routes.sessions.settlement.SettlementRoute
import com.splitit.routes.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object Sessions

@Serializable
data object Settings

@Serializable
data class SessionDetails(val sessionId: String)

@Serializable
data class SessionForm(val sessionId: String? = null)

@Serializable
data class Participants(val sessionId: String)

@Serializable
data class Expenses(val sessionId: String, val openExpenseForm: Boolean = false)

@Serializable
data class Settlement(val sessionId: String)

private const val TRANSITION_DURATION_MS = 300

private fun NavBackStackEntry.isFadeDestination(): Boolean =
    destination.route == Settings::class.qualifiedName

private fun enterFor(initial: NavBackStackEntry, target: NavBackStackEntry): EnterTransition =
    if (initial.isFadeDestination() || target.isFadeDestination()) {
        fadeIn(tween(TRANSITION_DURATION_MS))
    } else {
        slideInHorizontally(
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            initialOffsetX = { it },
        )
    }

private fun exitFor(initial: NavBackStackEntry, target: NavBackStackEntry): ExitTransition =
    if (initial.isFadeDestination() || target.isFadeDestination()) {
        fadeOut(tween(TRANSITION_DURATION_MS))
    } else {
        slideOutHorizontally(
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            targetOffsetX = { -it },
        )
    }

private fun popEnterFor(initial: NavBackStackEntry, target: NavBackStackEntry): EnterTransition =
    if (initial.isFadeDestination() || target.isFadeDestination()) {
        fadeIn(tween(TRANSITION_DURATION_MS))
    } else {
        slideInHorizontally(
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            initialOffsetX = { -it },
        )
    }

private fun popExitFor(initial: NavBackStackEntry, target: NavBackStackEntry): ExitTransition =
    if (initial.isFadeDestination() || target.isFadeDestination()) {
        fadeOut(tween(TRANSITION_DURATION_MS))
    } else {
        slideOutHorizontally(
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            targetOffsetX = { it },
        )
    }

@Composable
fun SplitItRoutes(
    settingsViewModel: SettingsViewModel,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Sessions,
        enterTransition = { enterFor(initialState, targetState) },
        exitTransition = { exitFor(initialState, targetState) },
        popEnterTransition = { popEnterFor(initialState, targetState) },
        popExitTransition = { popExitFor(initialState, targetState) },
    ) {
        composable<Sessions> {
            SessionsRoute(
                onCreate = { navController.navigate(SessionForm()) },
                onOpen = { sessionId -> navController.navigate(SessionDetails(sessionId.value)) },
                onEdit = { sessionId -> navController.navigate(SessionForm(sessionId.value)) },
                onSettings = { navController.navigate(Settings) },
            )
        }

        composable<SessionDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<SessionDetails>()
            SessionDetailsRoute(
                sessionId = SessionId(route.sessionId),
                onBack = { navController.popBackStack() },
                onParticipants = { navController.navigate(Participants(route.sessionId)) },
                onExpenses = { navController.navigate(Expenses(route.sessionId)) },
                onAddExpense = { navController.navigate(Expenses(route.sessionId, openExpenseForm = true)) },
                onSettlement = { navController.navigate(Settlement(route.sessionId)) },
            )
        }

        composable<SessionForm> { backStackEntry ->
            val route = backStackEntry.toRoute<SessionForm>()
            val sessionId = route.sessionId?.let(::SessionId)
            SessionFormRoute(
                sessionId = sessionId,
                onBack = { navController.popBackStack() },
                onSaved = { savedSessionId ->
                    if (sessionId == null) {
                        navController.navigate(SessionDetails(savedSessionId.value)) {
                            popUpTo<SessionForm> { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
            )
        }

        composable<Participants> { backStackEntry ->
            val route = backStackEntry.toRoute<Participants>()
            ParticipantsRoute(
                sessionId = SessionId(route.sessionId),
                onBack = { navController.popBackStack() },
            )
        }

        composable<Expenses> { backStackEntry ->
            val route = backStackEntry.toRoute<Expenses>()
            ExpensesRoute(
                sessionId = SessionId(route.sessionId),
                onBack = { navController.popBackStack() },
                openExpenseForm = route.openExpenseForm,
            )
        }

        composable<Settlement> { backStackEntry ->
            val route = backStackEntry.toRoute<Settlement>()
            SettlementRoute(
                sessionId = SessionId(route.sessionId),
                onBack = { navController.popBackStack() },
            )
        }

        composable<Settings> {
            SettingsRoute(
                onBack = { navController.popBackStack() },
                viewModel = settingsViewModel,
            )
        }
    }
}
