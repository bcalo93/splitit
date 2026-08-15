package com.splitit.routes.sessions.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.splitit.domain.model.Expense
import com.splitit.domain.model.Participant
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SessionId
import com.splitit.presentation.expenses.ExpensesUiState
import com.splitit.presentation.expenses.ExpensesViewModel
import com.splitit.presentation.expenses.formatMinorUnits
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.InlineErrorState
import com.splitit.ui.components.LoadingState
import com.splitit.ui.components.NoSearchResultsState
import com.splitit.ui.components.SearchField
import com.splitit.ui.components.participantColor
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExpensesRoute(
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
        onSearchQueryChange = viewModel::onSearchQueryChange,
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
private fun ExpensesScreen(
    state: ExpensesUiState,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
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
    val participantNames = remember(state.participants) {
        state.participants.associate { participant -> participant.id to participant.name }
    }

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
                InlineErrorState(message = state.errorMessage, onRetry = onRetry)
            }

            if (state.expenses.isNotEmpty() || state.searchQuery.isNotBlank()) {
                SearchField(
                    query = state.searchQuery,
                    label = "Search expenses",
                    onQueryChange = onSearchQueryChange,
                )
            }
            if (state.isLoading && state.expenses.isNotEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when {
                    state.isLoading && state.expenses.isEmpty() -> LoadingState(
                        modifier = Modifier.align(Alignment.Center),
                    )
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
                    state.visibleExpenses.isEmpty() -> NoSearchResultsState(
                        query = state.searchQuery,
                        entityName = "expenses",
                        onClear = { onSearchQueryChange("") },
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = state.visibleExpenses,
                            key = { it.id.value },
                            contentType = { "expense" },
                        ) { expense ->
                            ExpenseRow(
                                expense = expense,
                                participantNames = participantNames,
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
                label = {
                    Text("Amount (${state.editingCurrencyCode ?: state.defaultCurrencyCode})")
                },
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
    participantNames: Map<ParticipantId, String>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val payerName = participantNames[expense.payerId] ?: "Unknown"
    val splitNames = expense.participantShares.joinToString(", ") { share ->
        participantNames[share.participantId] ?: "Unknown"
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
