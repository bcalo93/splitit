package com.splitit.routes.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.model.ExpenseSession
import com.splitit.domain.value.SessionId
import com.splitit.presentation.sessions.SessionListUiState
import com.splitit.presentation.sessions.SessionListViewModel
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.InlineErrorState
import com.splitit.ui.components.LoadingState
import com.splitit.ui.components.NoSearchResultsState
import com.splitit.ui.components.SearchField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.app_name
import splitit.composeapp.generated.resources.cancel
import splitit.composeapp.generated.resources.create_session
import splitit.composeapp.generated.resources.delete
import splitit.composeapp.generated.resources.delete_session_message
import splitit.composeapp.generated.resources.delete_session_title
import splitit.composeapp.generated.resources.edit
import splitit.composeapp.generated.resources.entity_sessions
import splitit.composeapp.generated.resources.new_action
import splitit.composeapp.generated.resources.no_sessions_yet
import splitit.composeapp.generated.resources.search_sessions
import splitit.composeapp.generated.resources.session_summary
import splitit.composeapp.generated.resources.settings

@Composable
fun SessionsRoute(
    onCreate: () -> Unit,
    onOpen: (SessionId) -> Unit,
    onEdit: (SessionId) -> Unit,
    onSettings: () -> Unit,
    viewModel: SessionListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    SessionListScreen(
        state = state,
        onCreate = onCreate,
        onOpen = onOpen,
        onEdit = onEdit,
        onSettings = onSettings,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onDelete = viewModel::delete,
        onRetry = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionListScreen(
    state: SessionListUiState,
    onCreate: () -> Unit,
    onOpen: (SessionId) -> Unit,
    onEdit: (SessionId) -> Unit,
    onSettings: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDelete: (SessionId) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                actions = {
                    TextButton(onClick = onSettings) {
                        Text(stringResource(Res.string.settings))
                    }
                    Button(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = onCreate,
                    ) {
                        Text(stringResource(Res.string.new_action))
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            when {
                state.isLoading && state.sessions.isEmpty() -> LoadingState(
                    modifier = Modifier.align(Alignment.Center),
                )
                state.errorMessage != null && state.sessions.isEmpty() -> ErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.sessions.isNotEmpty() || state.searchQuery.isNotBlank()) {
                        SearchField(
                            query = state.searchQuery,
                            label = stringResource(Res.string.search_sessions),
                            onQueryChange = onSearchQueryChange,
                        )
                    }
                    if (state.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    state.errorMessage?.let { message ->
                        InlineErrorState(message = message, onRetry = onRetry)
                    }
                    when {
                        state.sessions.isEmpty() -> EmptySessionsState(
                            onCreate = onCreate,
                            modifier = Modifier.weight(1f),
                        )
                        state.visibleSessions.isEmpty() -> NoSearchResultsState(
                            query = state.searchQuery,
                            entityName = stringResource(Res.string.entity_sessions),
                            onClear = { onSearchQueryChange("") },
                            modifier = Modifier.weight(1f),
                        )
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = state.visibleSessions,
                                key = { it.id.value },
                                contentType = { "session" },
                            ) { session ->
                                SessionRow(
                                    session = session,
                                    onOpen = { onOpen(session.id) },
                                    onEdit = { onEdit(session.id) },
                                    onDelete = { onDelete(session.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: ExpenseSession,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            session.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = stringResource(Res.string.session_summary, session.participantIds.size, session.expenseIds.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) {
                    Text(stringResource(Res.string.edit))
                }
                TextButton(onClick = { showDeleteConfirmation = true }) {
                    Text(stringResource(Res.string.delete))
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(Res.string.delete_session_title)) },
            text = { Text(stringResource(Res.string.delete_session_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                ) {
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun EmptySessionsState(
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.no_sessions_yet),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Button(onClick = onCreate) {
            Text(stringResource(Res.string.create_session))
        }
    }
}
