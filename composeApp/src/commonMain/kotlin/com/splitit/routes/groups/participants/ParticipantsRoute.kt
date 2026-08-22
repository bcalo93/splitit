package com.splitit.routes.groups.participants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.model.Participant
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId
import com.splitit.presentation.participants.ParticipantsUiState
import com.splitit.presentation.participants.ParticipantsViewModel
import com.splitit.ui.components.AvatarBubble
import com.splitit.ui.components.ColorSelector
import com.splitit.ui.components.ConfirmDeleteDialog
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.FormTextField
import com.splitit.ui.components.LoadingState
import com.splitit.ui.components.ParticipantRow
import com.splitit.ui.components.PrimaryButton
import com.splitit.ui.components.SplitItIcons
import com.splitit.ui.components.SplitItScaffold
import com.splitit.ui.components.SplitItTopBar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.add
import splitit.composeapp.generated.resources.add_participant
import splitit.composeapp.generated.resources.edit
import splitit.composeapp.generated.resources.edit_participant
import splitit.composeapp.generated.resources.name
import splitit.composeapp.generated.resources.participants
import splitit.composeapp.generated.resources.participants_empty_body
import splitit.composeapp.generated.resources.participants_empty_title
import splitit.composeapp.generated.resources.remove
import splitit.composeapp.generated.resources.remove_participant_message
import splitit.composeapp.generated.resources.remove_participant_title
import splitit.composeapp.generated.resources.save
import splitit.composeapp.generated.resources.saving

@Composable
fun ParticipantsRoute(
    groupId: GroupId,
    onBack: () -> Unit,
    viewModel: ParticipantsViewModel = koinViewModel(
        parameters = { parametersOf(groupId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var sheetVisible by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Participant?>(null) }

    LaunchedEffect(groupId) {
        viewModel.refresh()
    }
    LaunchedEffect(state.saveSucceeded) {
        if (state.saveSucceeded) {
            viewModel.consumeSaveSuccess()
            sheetVisible = false
        }
    }

    ParticipantsScreen(
        state = state,
        onBack = onBack,
        onAddClick = {
            viewModel.startAdding()
            sheetVisible = true
        },
        onEditClick = { participant ->
            viewModel.startEditing(participant)
            sheetVisible = true
        },
        onDeleteClick = { participant -> pendingDelete = participant },
        onRetry = viewModel::refresh,
    )

    if (sheetVisible) {
        ParticipantSheet(
            state = state,
            onDismiss = {
                sheetVisible = false
                viewModel.cancelEditing()
            },
            onNameChange = viewModel::onNameChange,
            onColorSelected = viewModel::onColorSelected,
            onSave = viewModel::save,
        )
    }

    pendingDelete?.let { participant ->
        ConfirmDeleteDialog(
            title = stringResource(Res.string.remove_participant_title),
            message = stringResource(Res.string.remove_participant_message),
            confirmLabel = stringResource(Res.string.remove),
            onConfirm = {
                pendingDelete = null
                viewModel.delete(participant.id)
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun ParticipantsScreen(
    state: ParticipantsUiState,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Participant) -> Unit,
    onDeleteClick: (Participant) -> Unit,
    onRetry: () -> Unit,
) {
    SplitItScaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            SplitItTopBar(
                title = stringResource(Res.string.participants),
                onBack = onBack,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        painter = painterResource(SplitItIcons.PersonAdd),
                        contentDescription = null,
                    )
                },
                text = { Text(stringResource(Res.string.add_participant)) },
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
                state.participants.isEmpty() -> ParticipantsEmptyState(
                    onAddClick = onAddClick,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 88.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.participants,
                        key = { it.id.value },
                        contentType = { "participant" },
                    ) { participant ->
                        ParticipantRowItem(
                            participant = participant,
                            onEdit = { onEditClick(participant) },
                            onDelete = { onDeleteClick(participant) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantRowItem(
    participant: Participant,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        ParticipantRow(
            name = participant.name,
            colorHex = participant.avatarColor,
            onMoreClick = { menuExpanded = true },
        )
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.edit)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(SplitItIcons.Edit),
                        contentDescription = null,
                    )
                },
                onClick = {
                    menuExpanded = false
                    onEdit()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.remove)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(SplitItIcons.Delete),
                        contentDescription = null,
                    )
                },
                onClick = {
                    menuExpanded = false
                    onDelete()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantSheet(
    state: ParticipantsUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    onSave: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val isEditing = state.editingParticipantId != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(
                    if (isEditing) Res.string.edit_participant else Res.string.add_participant,
                ),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                AvatarBubble(
                    name = state.name,
                    colorHex = state.selectedColor,
                    size = 48.dp,
                )
            }

            FormTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = stringResource(Res.string.name),
                error = state.nameError,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )

            ColorSelector(
                selectedColor = state.selectedColor,
                onColorSelected = onColorSelected,
            )

            PrimaryButton(
                text = if (state.isSaving) {
                    stringResource(Res.string.saving)
                } else if (isEditing) {
                    stringResource(Res.string.save)
                } else {
                    stringResource(Res.string.add)
                },
                enabled = state.name.isNotBlank() && !state.isSaving && !state.isLoading,
                isLoading = state.isSaving,
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun ParticipantsEmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ParticipantsEmptyIllustration()
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.participants_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.participants_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = stringResource(Res.string.add_participant),
            onClick = onAddClick,
        )
    }
}

@Composable
private fun ParticipantsEmptyIllustration(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-12).dp),
    ) {
        GhostAvatar(
            size = 56.dp,
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        GhostAvatar(
            size = 56.dp,
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        GhostAvatar(
            size = 56.dp,
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GhostAvatar(
    size: androidx.compose.ui.unit.Dp,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(container, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(SplitItIcons.PersonAdd),
            contentDescription = null,
            tint = content,
        )
    }
}
