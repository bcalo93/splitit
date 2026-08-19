package com.splitit.routes.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.value.GroupId
import com.splitit.presentation.groups.GroupFormUiState
import com.splitit.presentation.groups.GroupFormViewModel
import com.splitit.ui.components.FormTextField
import com.splitit.ui.components.Skeleton
import com.splitit.ui.components.SplitItIcons
import com.splitit.ui.components.SplitItScaffold
import com.splitit.ui.components.SplitItTopBar
import com.splitit.ui.theme.LocalSplitItSpacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.cd_close
import splitit.composeapp.generated.resources.description
import splitit.composeapp.generated.resources.edit_group
import splitit.composeapp.generated.resources.group_description_hint
import splitit.composeapp.generated.resources.name
import splitit.composeapp.generated.resources.new_group
import splitit.composeapp.generated.resources.save

@Composable
fun GroupFormRoute(
    groupId: GroupId?,
    onBack: () -> Unit,
    onSaved: (GroupId) -> Unit,
    viewModel: GroupFormViewModel = koinViewModel(
        parameters = { parametersOf(groupId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedGroupId) {
        state.savedGroupId?.let {
            viewModel.consumeSavedGroup()
            onSaved(it)
        }
    }

    GroupFormScreen(
        state = state,
        isEditing = groupId != null,
        onBack = onBack,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onTitleBlur = viewModel::onTitleBlur,
        onSave = viewModel::save,
    )
}

@Composable
private fun GroupFormScreen(
    state: GroupFormUiState,
    isEditing: Boolean,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTitleBlur: () -> Unit,
    onSave: () -> Unit,
) {
    val spacing = LocalSplitItSpacing.current
    val canSave = state.title.isNotBlank() && !state.isSaving && !state.isLoading

    SplitItScaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            SplitItTopBar(
                title = stringResource(
                    if (isEditing) Res.string.edit_group else Res.string.new_group,
                ),
                onBack = onBack,
                navigationIcon = SplitItIcons.Close,
                navigationContentDescription = stringResource(Res.string.cd_close),
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = canSave,
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(spacing.sm))
                        }
                        Text(stringResource(Res.string.save))
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = spacing.lg, vertical = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.xl),
        ) {
            if (state.isLoading) {
                Skeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                )
                Skeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp),
                )
            } else {
                FormTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    label = stringResource(Res.string.name),
                    leadingIcon = SplitItIcons.Group,
                    error = state.titleError,
                    maxLength = 50,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                onTitleBlur()
                            }
                        },
                )
                FormTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = stringResource(Res.string.description),
                    placeholder = stringResource(Res.string.group_description_hint),
                    singleLine = false,
                    minLines = 4,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
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
