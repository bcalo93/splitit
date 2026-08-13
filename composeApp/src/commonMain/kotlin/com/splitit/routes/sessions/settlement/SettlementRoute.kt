package com.splitit.routes.sessions.settlement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.model.Balance
import com.splitit.domain.model.SettlementTransfer
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SessionId
import com.splitit.presentation.expenses.formatMinorUnits
import com.splitit.presentation.settlement.SettlementUiState
import com.splitit.presentation.settlement.SettlementViewModel
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.InlineErrorState
import com.splitit.ui.components.LoadingState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SettlementRoute(
    sessionId: SessionId,
    onBack: () -> Unit,
    viewModel: SettlementViewModel = koinViewModel(
        key = "settlement-${sessionId.value}",
        parameters = { parametersOf(sessionId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(sessionId) {
        viewModel.refresh()
    }

    SettlementScreen(
        state = state,
        onBack = onBack,
        onGenerate = viewModel::generate,
        onRetry = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettlementScreen(
    state: SettlementUiState,
    onBack: () -> Unit,
    onGenerate: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Settlement") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
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
                    onGenerate = onGenerate,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun SettlementContent(
    state: SettlementUiState,
    onGenerate: () -> Unit,
    onRetry: () -> Unit,
) {
    val participantNames = remember(state.participants) {
        state.participants.associate { participant -> participant.id to participant.name }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = if (state.settlement == null) {
                    "Turn the current balances into a short list of payments."
                } else {
                    "Payments needed to settle this session"
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (state.isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        if (state.isSettlementStale) {
            item {
                Text(
                    text = "The saved settlement is out of date. Regenerate it to include recent changes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        state.errorMessage?.let {
            item {
                InlineErrorState(message = it, onRetry = onRetry)
            }
        }

        item {
            Button(
                enabled = state.canGenerateSettlement && !state.isGenerating,
                onClick = onGenerate,
            ) {
                Text(
                    when {
                        state.isGenerating -> "Generating"
                        state.settlement == null -> "Generate settlement"
                        else -> "Regenerate settlement"
                    },
                )
            }
        }

        if (!state.canGenerateSettlement) {
            item {
                Text(
                    text = "Add at least two participants and one expense before generating a settlement.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.balances.isNotEmpty()) {
            item {
                Text(
                    text = "Balances",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(
                items = state.balances,
                key = { it.participantId.value },
                contentType = { "balance" },
            ) { balance ->
                BalanceRow(balance = balance, participantNames = participantNames)
            }
        }

        state.settlement?.let { settlement ->
            item {
                Text(
                    text = "Transfers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (settlement.transfers.isEmpty()) {
                item {
                    Text(
                        text = "Everyone is settled up.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(
                    items = settlement.transfers,
                    key = { it.id.value },
                    contentType = { "transfer" },
                ) { transfer ->
                    SettlementTransferRow(
                        transfer = transfer,
                        participantNames = participantNames,
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceRow(
    balance: Balance,
    participantNames: Map<ParticipantId, String>,
) {
    val name = participantNames[balance.participantId] ?: "Unknown"
    val minorUnits = balance.amount.minorUnits
    val amount = formatMinorUnits(if (minorUnits < 0) -minorUnits else minorUnits)
    val message = when {
        minorUnits > 0 -> "$name receives ${balance.amount.currencyCode} $amount"
        minorUnits < 0 -> "$name owes ${balance.amount.currencyCode} $amount"
        else -> "$name is settled"
    }

    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun SettlementTransferRow(
    transfer: SettlementTransfer,
    participantNames: Map<ParticipantId, String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "${participantNames[transfer.fromParticipantId] ?: "Unknown"} pays " +
                    (participantNames[transfer.toParticipantId] ?: "Unknown"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${transfer.amount.currencyCode} ${formatMinorUnits(transfer.amount.minorUnits)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
