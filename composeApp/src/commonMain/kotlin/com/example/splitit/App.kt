package com.example.splitit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitit.domain.model.ExpenseSession
import com.example.splitit.domain.value.SessionId
import com.example.splitit.presentation.sessiondetail.SessionDetailsUiState
import com.example.splitit.presentation.sessiondetail.SessionDetailsViewModel
import com.example.splitit.presentation.sessions.SessionFormUiState
import com.example.splitit.presentation.sessions.SessionFormViewModel
import com.example.splitit.presentation.sessions.SessionListUiState
import com.example.splitit.presentation.sessions.SessionListViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val ROUTE_SESSIONS = "sessions"
private const val ROUTE_DETAILS = "details"
private const val ROUTE_FORM = "form"

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
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
                        )
                    }
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
                    SessionListRoute(
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
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionListRoute(
    onCreate: () -> Unit,
    onOpen: (SessionId) -> Unit,
    onEdit: (SessionId) -> Unit,
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
        onDelete = viewModel::delete,
        onRetry = viewModel::refresh,
    )
}

@Composable
private fun SessionFormRoute(
    sessionId: SessionId?,
    formKey: Int,
    onBack: () -> Unit,
    onSaved: (SessionId) -> Unit,
    viewModel: SessionFormViewModel = koinViewModel(
        key = "session-form-${sessionId?.value ?: "new"}-$formKey",
        parameters = { parametersOf(sessionId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedSessionId) {
        state.savedSessionId?.let {
            viewModel.consumeSavedSession()
            onSaved(it)
        }
    }

    SessionFormScreen(
        state = state,
        isEditing = sessionId != null,
        onBack = onBack,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSave = viewModel::save,
    )
}

@Composable
private fun SessionDetailsRoute(
    sessionId: SessionId,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: SessionDetailsViewModel = koinViewModel(
        key = "session-details-${sessionId.value}",
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
    onDelete: (SessionId) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text("SplitIt") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                actions = {
                    Button(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = onCreate,
                    ) {
                        Text("New")
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
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.errorMessage != null -> ErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                state.sessions.isEmpty() -> EmptySessionsState(
                    onCreate = onCreate,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.sessions, key = { it.id.value }) { session ->
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
                text = "${session.participantIds.size} participants | ${session.expenseIds.size} expenses",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = { showDeleteConfirmation = true }) {
                    Text("Delete")
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete session?") },
            text = { Text("This removes the session and its local data from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionFormScreen(
    state: SessionFormUiState,
    isEditing: Boolean,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit session" else "New session") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                actions = {
                    Button(
                        modifier = Modifier.padding(end = 16.dp),
                        enabled = !state.isSaving && !state.isLoading,
                        onClick = onSave,
                    ) {
                        Text(if (state.isSaving) "Saving" else "Save")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                    singleLine = true,
                    isError = state.titleError != null,
                    supportingText = state.titleError?.let { message -> { Text(message) } },
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp),
                    label = { Text("Description") },
                    maxLines = 4,
                )
                state.errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionDetailsScreen(
    state: SessionDetailsUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Session") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                actions = {
                    TextButton(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = onEdit,
                    ) {
                        Text("Edit")
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
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.errorMessage != null -> ErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                state.details != null -> {
                    val details = state.details
                    Column(
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
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
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryBlock(label = "Participants", value = details.participants.size.toString())
                            SummaryBlock(label = "Expenses", value = details.expenses.size.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBlock(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .width(144.dp)
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
            text = "No sessions yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Button(onClick = onCreate) {
            Text("Create session")
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        OutlinedButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}
