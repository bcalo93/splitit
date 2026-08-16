package com.splitit.routes.sessions.participants

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.splitit.domain.model.Participant
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SessionId
import com.splitit.presentation.participants.ParticipantColors
import com.splitit.presentation.participants.ParticipantsUiState
import com.splitit.presentation.participants.ParticipantsViewModel
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.InlineErrorState
import com.splitit.ui.components.LoadingState
import com.splitit.ui.components.participantColor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.add_first_participant
import splitit.composeapp.generated.resources.add_participant
import splitit.composeapp.generated.resources.back
import splitit.composeapp.generated.resources.cancel
import splitit.composeapp.generated.resources.delete
import splitit.composeapp.generated.resources.edit
import splitit.composeapp.generated.resources.edit_participant
import splitit.composeapp.generated.resources.name
import splitit.composeapp.generated.resources.no_participants_yet
import splitit.composeapp.generated.resources.participants
import splitit.composeapp.generated.resources.remove
import splitit.composeapp.generated.resources.remove_participant_message
import splitit.composeapp.generated.resources.remove_participant_title
import splitit.composeapp.generated.resources.save
import splitit.composeapp.generated.resources.saving

@Composable
fun ParticipantsRoute(
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
                title = { Text(stringResource(Res.string.participants)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(Res.string.back))
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
                InlineErrorState(message = state.errorMessage, onRetry = onRetry)
            }
            if (state.isLoading && state.participants.isNotEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                    state.participants.isEmpty() -> EmptyParticipantsState(
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = state.participants,
                            key = { it.id.value },
                            contentType = { "participant" },
                        ) { participant ->
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
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (state.editingParticipantId == null) stringResource(Res.string.add_participant) else stringResource(Res.string.edit_participant),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.name)) },
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
                    Text(if (state.isSaving) stringResource(Res.string.saving) else stringResource(Res.string.save))
                }
                if (state.editingParticipantId != null) {
                    TextButton(onClick = onCancelEdit) {
                        Text(stringResource(Res.string.cancel))
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
        shape = MaterialTheme.shapes.medium,
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
                Text(stringResource(Res.string.edit))
            }
            TextButton(onClick = { showDeleteConfirmation = true }) {
                Text(stringResource(Res.string.delete))
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(Res.string.remove_participant_title)) },
            text = { Text(stringResource(Res.string.remove_participant_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                ) {
                    Text(stringResource(Res.string.remove))
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
private fun EmptyParticipantsState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.no_participants_yet),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(Res.string.add_first_participant),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
