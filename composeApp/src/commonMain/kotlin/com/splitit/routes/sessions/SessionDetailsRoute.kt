package com.splitit.routes.sessions

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.value.SessionId
import com.splitit.presentation.sessiondetail.SessionDetailsUiState
import com.splitit.presentation.sessiondetail.SessionDetailsViewModel
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.InlineErrorState
import com.splitit.ui.components.LoadingState
import com.splitit.ui.components.ArrowBackIcon
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.back
import splitit.composeapp.generated.resources.edit
import splitit.composeapp.generated.resources.expenses
import splitit.composeapp.generated.resources.generate_settlement
import splitit.composeapp.generated.resources.manage_expenses
import splitit.composeapp.generated.resources.manage_participants
import splitit.composeapp.generated.resources.participants
import splitit.composeapp.generated.resources.session
import splitit.composeapp.generated.resources.settlement_needs_regeneration
import splitit.composeapp.generated.resources.settlement_up_to_date
import splitit.composeapp.generated.resources.view_settlement

@Composable
fun SessionDetailsRoute(
    sessionId: SessionId,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onParticipants: () -> Unit,
    onExpenses: () -> Unit,
    onSettlement: () -> Unit,
    viewModel: SessionDetailsViewModel = koinViewModel(
        parameters = { parametersOf(sessionId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(sessionId) {
        viewModel.refresh()
    }

    SessionDetailsScreen(
        state = state,
        onBack = onBack,
        onEdit = onEdit,
        onParticipants = onParticipants,
        onExpenses = onExpenses,
        onSettlement = onSettlement,
        onRetry = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionDetailsScreen(
    state: SessionDetailsUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onParticipants: () -> Unit,
    onExpenses: () -> Unit,
    onSettlement: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.session)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = ArrowBackIcon,
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
                actions = {
                    TextButton(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = onEdit,
                    ) {
                        Text(stringResource(Res.string.edit))
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
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        if (state.isLoading) {
                            item {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                        state.errorMessage?.let { message ->
                            item {
                                InlineErrorState(message = message, onRetry = onRetry)
                            }
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                Text(
                                    text = details.session.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                details.session.description?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    SummaryBlock(
                                        modifier = Modifier.weight(1f),
                                        label = stringResource(Res.string.participants),
                                        value = details.participants.size.toString(),
                                    )
                                    SummaryBlock(
                                        modifier = Modifier.weight(1f),
                                        label = stringResource(Res.string.expenses),
                                        value = details.expenses.size.toString(),
                                    )
                                }
                                Button(onClick = onParticipants) {
                                    Text(stringResource(Res.string.manage_participants))
                                }
                                Button(
                                    enabled = details.participants.isNotEmpty(),
                                    onClick = onExpenses,
                                ) {
                                    Text(stringResource(Res.string.manage_expenses))
                                }
                                details.latestSettlement?.let {
                                    Text(
                                        text = if (details.isSettlementStale) {
                                            stringResource(Res.string.settlement_needs_regeneration)
                                        } else {
                                            stringResource(Res.string.settlement_up_to_date)
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (details.isSettlementStale) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                                Button(onClick = onSettlement) {
                                    Text(
                                        if (details.latestSettlement == null) {
                                            stringResource(Res.string.generate_settlement)
                                        } else {
                                            stringResource(Res.string.view_settlement)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBlock(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
