package com.example.splitit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitit.domain.model.Expense
import com.example.splitit.domain.model.ExpenseSession
import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.value.ExpenseId
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import com.example.splitit.presentation.expenses.DefaultCurrencyCode
import com.example.splitit.presentation.expenses.ExpensesUiState
import com.example.splitit.presentation.expenses.ExpensesViewModel
import com.example.splitit.presentation.expenses.formatMinorUnits
import com.example.splitit.presentation.participants.ParticipantColors
import com.example.splitit.presentation.participants.ParticipantsUiState
import com.example.splitit.presentation.participants.ParticipantsViewModel
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
private const val ROUTE_PARTICIPANTS = "participants"
private const val ROUTE_EXPENSES = "expenses"

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
                            onParticipants = {
                                routeSessionId = sessionId
                                route = ROUTE_PARTICIPANTS
                            },
                            onExpenses = {
                                routeSessionId = sessionId
                                route = ROUTE_EXPENSES
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
    onParticipants: () -> Unit,
    onExpenses: () -> Unit,
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
        onParticipants = onParticipants,
        onExpenses = onExpenses,
        onRetry = viewModel::refresh,
    )
}

@Composable
private fun ParticipantsRoute(
    sessionId: SessionId,
    onBack: () -> Unit,
    viewModel: ParticipantsViewModel = koinViewModel(
        key = "participants-${sessionId.value}",
        parameters = { parametersOf(sessionId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(sessionId) {
        viewModel.refresh()
    }

    ParticipantsScreen(
        state = state,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onColorSelected = viewModel::onColorSelected,
        onSave = viewModel::save,
        onEdit = viewModel::startEditing,
        onCancelEdit = viewModel::cancelEditing,
        onDelete = viewModel::delete,
        onRetry = viewModel::refresh,
    )
}

@Composable
private fun ExpensesRoute(
    sessionId: SessionId,
    onBack: () -> Unit,
    viewModel: ExpensesViewModel = koinViewModel(
        key = "expenses-${sessionId.value}",
        parameters = { parametersOf(sessionId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(sessionId) {
        viewModel.refresh()
    }

    ExpensesScreen(
        state = state,
        onBack = onBack,
        onTitleChange = viewModel::onTitleChange,
        onAmountChange = viewModel::onAmountChange,
        onNoteChange = viewModel::onNoteChange,
        onPayerSelected = viewModel::onPayerSelected,
        onParticipantToggled = viewModel::onParticipantToggled,
        onSave = viewModel::save,
        onEdit = viewModel::startEditing,
        onCancelEdit = viewModel::cancelEditing,
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
    onParticipants: () -> Unit,
    onExpenses: () -> Unit,
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
                        Button(onClick = onParticipants) {
                            Text("Manage participants")
                        }
                        Button(
                            enabled = details.participants.isNotEmpty(),
                            onClick = onExpenses,
                        ) {
                            Text("Manage expenses")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpensesScreen(
    state: ExpensesUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onPayerSelected: (ParticipantId) -> Unit,
    onParticipantToggled: (ParticipantId) -> Unit,
    onSave: () -> Unit,
    onEdit: (Expense) -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: (ExpenseId) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ExpenseForm(
                state = state,
                onTitleChange = onTitleChange,
                onAmountChange = onAmountChange,
                onNoteChange = onNoteChange,
                onPayerSelected = onPayerSelected,
                onParticipantToggled = onParticipantToggled,
                onSave = onSave,
                onCancelEdit = onCancelEdit,
            )

            if (state.errorMessage != null && state.expenses.isNotEmpty()) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.errorMessage != null && state.expenses.isEmpty() -> ErrorState(
                        message = state.errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    state.participants.isEmpty() -> EmptyExpensesWithoutParticipantsState(
                        modifier = Modifier.align(Alignment.Center),
                    )
                    state.expenses.isEmpty() -> EmptyExpensesState(
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.expenses, key = { it.id.value }) { expense ->
                            ExpenseRow(
                                expense = expense,
                                participants = state.participants,
                                onEdit = { onEdit(expense) },
                                onDelete = { onDelete(expense.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseForm(
    state: ExpensesUiState,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onPayerSelected: (ParticipantId) -> Unit,
    onParticipantToggled: (ParticipantId) -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (state.editingExpenseId == null) "Add expense" else "Edit expense",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true,
                isError = state.titleError != null,
                supportingText = state.titleError?.let { message -> { Text(message) } },
            )
            OutlinedTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount ($DefaultCurrencyCode)") },
                singleLine = true,
                isError = state.amountError != null,
                supportingText = state.amountError?.let { message -> { Text(message) } },
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                label = { Text("Note") },
                maxLines = 3,
            )
            ParticipantChoiceSection(
                title = "Paid by",
                participants = state.participants,
                selectedIds = state.payerId?.let { setOf(it) } ?: emptySet(),
                onParticipantSelected = onPayerSelected,
                singleSelection = true,
                errorMessage = state.payerError,
            )
            ParticipantChoiceSection(
                title = "Split between",
                participants = state.participants,
                selectedIds = state.selectedParticipantIds,
                onParticipantSelected = onParticipantToggled,
                singleSelection = false,
                errorMessage = state.participantsError,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !state.isSaving && !state.isLoading && state.participants.isNotEmpty(),
                    onClick = onSave,
                ) {
                    Text(if (state.isSaving) "Saving" else "Save")
                }
                if (state.editingExpenseId != null) {
                    TextButton(onClick = onCancelEdit) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantChoiceSection(
    title: String,
    participants: List<Participant>,
    selectedIds: Set<ParticipantId>,
    onParticipantSelected: (ParticipantId) -> Unit,
    singleSelection: Boolean,
    errorMessage: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        participants.forEach { participant ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onParticipantSelected(participant.id) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Checkbox(
                    checked = participant.id in selectedIds,
                    onCheckedChange = { onParticipantSelected(participant.id) },
                )
                Surface(
                    modifier = Modifier.size(24.dp),
                    color = participantColor(participant.avatarColor),
                    shape = CircleShape,
                ) {}
                Text(
                    modifier = Modifier.weight(1f),
                    text = participant.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (singleSelection && participants.isEmpty()) {
            Text(
                text = "Add participants before creating expenses.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    participants: List<Participant>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val payerName = participantName(participants, expense.payerId)
    val splitNames = expense.participantShares.joinToString(", ") { share ->
        participantName(participants, share.participantId)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${expense.amount.currencyCode} ${formatMinorUnits(expense.amount.minorUnits)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = { showDeleteConfirmation = true }) {
                    Text("Delete")
                }
            }
            Text(
                text = "Paid by $payerName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Split: $splitNames",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            expense.note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete expense?") },
            text = { Text("This removes the expense from this session.") },
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
private fun ParticipantsScreen(
    state: ParticipantsUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    onSave: () -> Unit,
    onEdit: (Participant) -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: (ParticipantId) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Participants") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ParticipantForm(
                state = state,
                onNameChange = onNameChange,
                onColorSelected = onColorSelected,
                onSave = onSave,
                onCancelEdit = onCancelEdit,
            )

            if (state.errorMessage != null && state.participants.isNotEmpty()) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.errorMessage != null && state.participants.isEmpty() -> ErrorState(
                        message = state.errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    state.participants.isEmpty() -> EmptyParticipantsState(
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.participants, key = { it.id.value }) { participant ->
                            ParticipantRow(
                                participant = participant,
                                onEdit = { onEdit(participant) },
                                onDelete = { onDelete(participant.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantForm(
    state: ParticipantsUiState,
    onNameChange: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (state.editingParticipantId == null) "Add participant" else "Edit participant",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = state.nameError?.let { message -> { Text(message) } },
            )
            ColorSelector(
                selectedColor = state.selectedColor,
                onColorSelected = onColorSelected,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !state.isSaving && !state.isLoading,
                    onClick = onSave,
                ) {
                    Text(if (state.isSaving) "Saving" else "Save")
                }
                if (state.editingParticipantId != null) {
                    TextButton(onClick = onCancelEdit) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ParticipantColors.forEach { color ->
            val isSelected = color == selectedColor
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                    )
                    .clickable { onColorSelected(color) },
                color = participantColor(color),
                shape = CircleShape,
                content = {},
            )
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: Participant,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = participantColor(participant.avatarColor),
                shape = CircleShape,
            ) {}
            Text(
                modifier = Modifier.weight(1f),
                text = participant.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            OutlinedButton(onClick = onEdit) {
                Text("Edit")
            }
            TextButton(onClick = { showDeleteConfirmation = true }) {
                Text("Delete")
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Remove participant?") },
            text = { Text("Participants used by expenses cannot be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                ) {
                    Text("Remove")
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

@Composable
private fun EmptyParticipantsState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "No participants yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Add the first person to start splitting expenses.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyExpensesWithoutParticipantsState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "No participants yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Add participants before creating expenses.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyExpensesState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "No expenses yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Add the first expense to split it equally.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun participantColor(color: String?): Color {
    return when (color) {
        "#2F80ED" -> Color(0xFF2F80ED)
        "#27AE60" -> Color(0xFF27AE60)
        "#EB5757" -> Color(0xFFEB5757)
        "#F2994A" -> Color(0xFFF2994A)
        "#9B51E0" -> Color(0xFF9B51E0)
        "#00A6A6" -> Color(0xFF00A6A6)
        else -> Color(0xFF2F80ED)
    }
}

private fun participantName(
    participants: List<Participant>,
    participantId: ParticipantId,
): String {
    return participants.firstOrNull { it.id == participantId }?.name ?: "Unknown"
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
