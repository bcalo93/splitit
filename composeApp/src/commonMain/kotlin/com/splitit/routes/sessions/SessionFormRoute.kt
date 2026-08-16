package com.splitit.routes.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.value.SessionId
import com.splitit.presentation.sessions.SessionFormUiState
import com.splitit.presentation.sessions.SessionFormViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.back
import splitit.composeapp.generated.resources.description
import splitit.composeapp.generated.resources.edit_session
import splitit.composeapp.generated.resources.name
import splitit.composeapp.generated.resources.new_session
import splitit.composeapp.generated.resources.save
import splitit.composeapp.generated.resources.saving

@Composable
fun SessionFormRoute(
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
                title = { Text(if (isEditing) stringResource(Res.string.edit_session) else stringResource(Res.string.new_session)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(Res.string.back))
                    }
                },
                actions = {
                    Button(
                        modifier = Modifier.padding(end = 16.dp),
                        enabled = !state.isSaving && !state.isLoading,
                        onClick = onSave,
                    ) {
                        Text(if (state.isSaving) stringResource(Res.string.saving) else stringResource(Res.string.save))
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
                    label = { Text(stringResource(Res.string.name)) },
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
                    label = { Text(stringResource(Res.string.description)) },
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
