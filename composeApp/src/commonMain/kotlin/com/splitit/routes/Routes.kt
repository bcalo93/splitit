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
import com.splitit.domain.value.GroupId
import com.splitit.presentation.settings.SettingsViewModel
import com.splitit.routes.groups.GroupDetailsRoute
import com.splitit.routes.groups.GroupFormRoute
import com.splitit.routes.groups.GroupsRoute
import com.splitit.routes.groups.expenses.ExpensesRoute
import com.splitit.routes.groups.participants.ParticipantsRoute
import com.splitit.routes.groups.settlement.SettlementRoute
import com.splitit.routes.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object Groups

@Serializable
data object Settings

@Serializable
data class GroupDetails(val groupId: String)

@Serializable
data class GroupForm(val groupId: String? = null)

@Serializable
data class Participants(val groupId: String)

@Serializable
data class Expenses(val groupId: String, val openExpenseForm: Boolean = false)

@Serializable
data class Settlement(val groupId: String)

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
        startDestination = Groups,
        enterTransition = { enterFor(initialState, targetState) },
        exitTransition = { exitFor(initialState, targetState) },
        popEnterTransition = { popEnterFor(initialState, targetState) },
        popExitTransition = { popExitFor(initialState, targetState) },
    ) {
        composable<Groups> {
            GroupsRoute(
                onCreate = { navController.navigate(GroupForm()) },
                onOpen = { groupId -> navController.navigate(GroupDetails(groupId.value)) },
                onEdit = { groupId -> navController.navigate(GroupForm(groupId.value)) },
                onSettings = { navController.navigate(Settings) },
            )
        }

        composable<GroupDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<GroupDetails>()
            GroupDetailsRoute(
                groupId = GroupId(route.groupId),
                onBack = { navController.popBackStack() },
                onParticipants = { navController.navigate(Participants(route.groupId)) },
                onExpenses = { navController.navigate(Expenses(route.groupId)) },
                onAddExpense = { navController.navigate(Expenses(route.groupId, openExpenseForm = true)) },
                onSettlement = { navController.navigate(Settlement(route.groupId)) },
            )
        }

        composable<GroupForm> { backStackEntry ->
            val route = backStackEntry.toRoute<GroupForm>()
            val groupId = route.groupId?.let(::GroupId)
            GroupFormRoute(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onSaved = { savedGroupId ->
                    if (groupId == null) {
                        navController.navigate(GroupDetails(savedGroupId.value)) {
                            popUpTo<GroupForm> { inclusive = true }
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
                groupId = GroupId(route.groupId),
                onBack = { navController.popBackStack() },
            )
        }

        composable<Expenses> { backStackEntry ->
            val route = backStackEntry.toRoute<Expenses>()
            ExpensesRoute(
                groupId = GroupId(route.groupId),
                onBack = { navController.popBackStack() },
                openExpenseForm = route.openExpenseForm,
            )
        }

        composable<Settlement> { backStackEntry ->
            val route = backStackEntry.toRoute<Settlement>()
            SettlementRoute(
                groupId = GroupId(route.groupId),
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
