package com.splitit.routes.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.usecase.GroupDetails
import com.splitit.domain.value.Money
import com.splitit.domain.value.GroupId
import com.splitit.presentation.groupdetail.GroupDetailsUiState
import com.splitit.presentation.groupdetail.GroupDetailsViewModel
import com.splitit.ui.components.AvatarStack
import com.splitit.ui.components.AvatarStackItem
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.InlineErrorState
import com.splitit.ui.components.LoadingState
import com.splitit.ui.components.MoneyText
import com.splitit.ui.components.MoneyTextVariant
import com.splitit.ui.components.SplitItIcons
import com.splitit.ui.components.SplitItScaffold
import com.splitit.ui.components.SplitItTopBar
import com.splitit.ui.components.StatusChip
import com.splitit.ui.components.StatusChipStyle
import com.splitit.ui.components.pressScale
import com.splitit.ui.theme.LocalSplitItSpacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.add_expense
import splitit.composeapp.generated.resources.expenses
import splitit.composeapp.generated.resources.generate_settlement
import splitit.composeapp.generated.resources.participants
import splitit.composeapp.generated.resources.settlement_balanced
import splitit.composeapp.generated.resources.settlement_pending_changes
import splitit.composeapp.generated.resources.total_spent
import splitit.composeapp.generated.resources.view_settlement

@Composable
fun GroupDetailsRoute(
    groupId: GroupId,
    onBack: () -> Unit,
    onParticipants: () -> Unit,
    onExpenses: () -> Unit,
    onAddExpense: () -> Unit,
    onSettlement: () -> Unit,
    viewModel: GroupDetailsViewModel = koinViewModel(
        parameters = { parametersOf(groupId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(groupId) {
        viewModel.refresh()
    }

    GroupDetailsScreen(
        state = state,
        onBack = onBack,
        onParticipants = onParticipants,
        onExpenses = onExpenses,
        onAddExpense = onAddExpense,
        onSettlement = onSettlement,
        onRetry = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailsScreen(
    state: GroupDetailsUiState,
    onBack: () -> Unit,
    onParticipants: () -> Unit,
    onExpenses: () -> Unit,
    onAddExpense: () -> Unit,
    onSettlement: () -> Unit,
    onRetry: () -> Unit,
) {
    val spacing = LocalSplitItSpacing.current
    SplitItScaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            SplitItTopBar(
                title = "",
                onBack = onBack,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        painter = painterResource(SplitItIcons.Add),
                        contentDescription = null,
                    )
                },
                text = { Text(stringResource(Res.string.add_expense)) },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading && state.details == null -> LoadingState(
                    modifier = Modifier.align(Alignment.Center),
                )
                state.errorMessage != null && state.details == null -> ErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                state.details != null -> state.details.let { details ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = spacing.md,
                            end = spacing.md,
                            top = 8.dp,
                            bottom = 96.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        state.errorMessage?.let { message ->
                            item {
                                InlineErrorState(message = message, onRetry = onRetry)
                            }
                        }
                        item {
                            GroupHeader(details)
                        }
                        item {
                            SummaryPanel(
                                details = details,
                                totalSpent = state.totalSpent,
                            )
                        }
                        item {
                            SettlementAction(
                                details = details,
                                onSettlement = onSettlement,
                            )
                        }
                        item {
                            ActionCards(
                                details = details,
                                onParticipants = onParticipants,
                                onExpenses = onExpenses,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(details: GroupDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AvatarStack(
            items = details.participants.map {
                AvatarStackItem(name = it.name, colorHex = it.avatarColor)
            },
            size = 40.dp,
        )
        Text(
            text = details.group.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        details.group.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SummaryPanel(
    details: GroupDetails,
    totalSpent: Money?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryMetric(
                    label = stringResource(Res.string.total_spent),
                    modifier = Modifier.weight(1f),
                ) {
                    if (totalSpent != null) {
                        MoneyText(
                            amount = totalSpent,
                            variant = MoneyTextVariant.Row,
                        )
                    } else {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                SummaryMetric(
                    label = stringResource(Res.string.expenses),
                    modifier = Modifier.weight(1f),
                ) {
                    MetricCount(details.expenses.size)
                }
                SummaryMetric(
                    label = stringResource(Res.string.participants),
                    modifier = Modifier.weight(1f),
                ) {
                    MetricCount(details.participants.size)
                }
            }

            SettlementStatus(details)
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        value()
    }
}

@Composable
private fun MetricCount(count: Int) {
    Text(
        text = count.toString(),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun SettlementStatus(details: GroupDetails) {
    val settlement = details.latestSettlement ?: return
    if (details.isSettlementStale) {
        StaleSettlementBanner()
    } else {
        StatusChip(
            style = StatusChipStyle.UpToDate,
            label = stringResource(Res.string.settlement_balanced),
        )
    }
}

@Composable
private fun StaleSettlementBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(SplitItIcons.WarningAmber),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                text = stringResource(Res.string.settlement_pending_changes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun SettlementAction(
    details: GroupDetails,
    onSettlement: () -> Unit,
) {
    Button(
        onClick = onSettlement,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CircleShape,
    ) {
        Icon(
            painter = painterResource(SplitItIcons.AccountBalanceWallet),
            contentDescription = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(
                if (details.latestSettlement == null) {
                    Res.string.generate_settlement
                } else {
                    Res.string.view_settlement
                },
            ),
        )
        if (details.isSettlementStale) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
            )
        }
    }
}

@Composable
private fun ActionCards(
    details: GroupDetails,
    onParticipants: () -> Unit,
    onExpenses: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ActionCard(
            modifier = Modifier.weight(1f),
            title = stringResource(Res.string.participants),
            count = details.participants.size,
            icon = SplitItIcons.Group,
            onClick = onParticipants,
        )
        ActionCard(
            modifier = Modifier.weight(1f),
            title = stringResource(Res.string.expenses),
            count = details.expenses.size,
            icon = SplitItIcons.ReceiptLong,
            onClick = onExpenses,
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    count: Int,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource),
        interactionSource = interactionSource,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
