package com.splitit.routes.groups.settlement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.value.GroupId
import com.splitit.presentation.settlement.SettlementUiState
import com.splitit.presentation.settlement.SettlementViewModel
import com.splitit.ui.components.BalanceBarChart
import com.splitit.ui.components.BalanceBarEntry
import com.splitit.ui.components.EmptyState
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.InlineErrorState
import com.splitit.ui.components.LoadingState
import com.splitit.ui.components.SplitItIcons
import com.splitit.ui.components.SplitItScaffold
import com.splitit.ui.components.SplitItTopBar
import com.splitit.ui.components.TransferCard
import com.splitit.ui.theme.LocalSplitItSpacing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.balances
import splitit.composeapp.generated.resources.everyone_settled
import splitit.composeapp.generated.resources.settlement
import splitit.composeapp.generated.resources.settlement_all_settled_title
import splitit.composeapp.generated.resources.settlement_empty_title
import splitit.composeapp.generated.resources.settlement_requirements
import splitit.composeapp.generated.resources.transfers
import splitit.composeapp.generated.resources.unknown

@Composable
fun SettlementRoute(
    groupId: GroupId,
    onBack: () -> Unit,
    viewModel: SettlementViewModel = koinViewModel(
        parameters = { parametersOf(groupId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(groupId) {
        viewModel.refresh()
    }

    SettlementScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::refresh,
    )
}

@Composable
private fun SettlementScreen(
    state: SettlementUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    SplitItScaffold(
        topBar = {
            SplitItTopBar(
                title = stringResource(Res.string.settlement),
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading && state.participants.isEmpty() -> LoadingState(
                    modifier = Modifier.align(Alignment.Center),
                )
                state.errorMessage != null && state.participants.isEmpty() -> ErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> SettlementContent(
                    state = state,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun SettlementContent(
    state: SettlementUiState,
    onRetry: () -> Unit,
) {
    val spacing = LocalSplitItSpacing.current
    val participantNames = remember(state.participants) {
        state.participants.associate { participant -> participant.id to participant.name }
    }
    val participantColors = remember(state.participants) {
        state.participants.associate { participant -> participant.id to participant.avatarColor }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = spacing.xs, end = spacing.xs, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        state.errorMessage?.let { message ->
            item {
                InlineErrorState(message = message, onRetry = onRetry)
            }
        }

        if (state.settlement == null) {
            item {
                SettlementEmptyState(canGenerate = state.canGenerateSettlement)
            }
        } else {
            if (state.balances.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(Res.string.balances))
                }
                item {
                    BalanceBarChart(
                        entries = state.balances.map { balance ->
                            BalanceBarEntry(
                                name = participantNames[balance.participantId]
                                    ?: stringResource(Res.string.unknown),
                                colorHex = participantColors[balance.participantId],
                                amount = balance.amount,
                            )
                        },
                    )
                }
            }

            val settlement = state.settlement
            if (settlement.transfers.isEmpty()) {
                item {
                    CelebrationState()
                }
            } else {
                item {
                    SectionHeader(title = stringResource(Res.string.transfers))
                }
                items(
                    items = settlement.transfers,
                    key = { it.id.value },
                    contentType = { "transfer" },
                ) { transfer ->
                    TransferCard(
                        fromName = participantNames[transfer.fromParticipantId]
                            ?: stringResource(Res.string.unknown),
                        fromColorHex = participantColors[transfer.fromParticipantId],
                        toName = participantNames[transfer.toParticipantId]
                            ?: stringResource(Res.string.unknown),
                        toColorHex = participantColors[transfer.toParticipantId],
                        amount = transfer.amount,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettlementEmptyState(canGenerate: Boolean) {
    EmptyState(
        title = stringResource(Res.string.settlement_empty_title),
        body = if (canGenerate) null else stringResource(Res.string.settlement_requirements),
        icon = SplitItIcons.AccountBalanceWallet,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CelebrationState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(SplitItIcons.Celebration),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        Text(
            text = stringResource(Res.string.settlement_all_settled_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.everyone_settled),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}
