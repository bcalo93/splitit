package com.splitit.routes.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.repository.ThemeMode
import com.splitit.presentation.settings.SettingsUiState
import com.splitit.presentation.settings.SettingsViewModel
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.LoadingState
import com.splitit.ui.components.ArrowBackIcon
import org.jetbrains.compose.resources.stringResource
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.back
import splitit.composeapp.generated.resources.currency_hint
import splitit.composeapp.generated.resources.default_currency
import splitit.composeapp.generated.resources.preferences_stored_locally
import splitit.composeapp.generated.resources.retry
import splitit.composeapp.generated.resources.save
import splitit.composeapp.generated.resources.saving
import splitit.composeapp.generated.resources.settings
import splitit.composeapp.generated.resources.settings_saved
import splitit.composeapp.generated.resources.theme
import splitit.composeapp.generated.resources.theme_dark
import splitit.composeapp.generated.resources.theme_dark_description
import splitit.composeapp.generated.resources.theme_light
import splitit.composeapp.generated.resources.theme_light_description
import splitit.composeapp.generated.resources.theme_system
import splitit.composeapp.generated.resources.theme_system_description

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onBack = onBack,
        onCurrencyCodeChange = viewModel::onCurrencyCodeChange,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onSave = viewModel::save,
        onRetry = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onCurrencyCodeChange: (String) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = ArrowBackIcon,
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
                actions = {
                    Button(
                        modifier = Modifier.padding(end = 16.dp),
                        enabled = !state.isLoading && !state.isSaving,
                        onClick = onSave,
                    ) {
                        Text(if (state.isSaving) stringResource(Res.string.saving) else stringResource(Res.string.save))
                    }
                },
            )
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = stringResource(Res.string.preferences_stored_locally),
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedTextField(
                    value = state.draftSettings.defaultCurrencyCode,
                    onValueChange = onCurrencyCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving,
                    label = { Text(stringResource(Res.string.default_currency)) },
                    supportingText = {
                        if (state.currencyError != null) {
                            Text(state.currencyError)
                        } else {
                            Text(stringResource(Res.string.currency_hint))
                        }
                    },
                    isError = state.currencyError != null,
                    singleLine = true,
                )
                Text(
                    text = stringResource(Res.string.theme),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                ThemeMode.entries.forEach { themeMode ->
                    ThemeModeOption(
                        themeMode = themeMode,
                        selected = state.draftSettings.themeMode == themeMode,
                        enabled = !state.isSaving,
                        onClick = { onThemeModeSelected(themeMode) },
                    )
                }
                state.errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (state.saveCompleted) {
                    Text(
                        text = stringResource(Res.string.settings_saved),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (state.errorMessage != null) {
                    OutlinedButton(onClick = onRetry) {
                        Text(stringResource(Res.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeModeOption(
    themeMode: ThemeMode,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = selected,
            enabled = enabled,
            onClick = onClick,
        )
        Column {
            Text(
                text = themeModeLabel(themeMode),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = themeModeDescription(themeMode),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun themeModeLabel(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.System -> stringResource(Res.string.theme_system)
        ThemeMode.Light -> stringResource(Res.string.theme_light)
        ThemeMode.Dark -> stringResource(Res.string.theme_dark)
    }
}

@Composable
private fun themeModeDescription(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.System -> stringResource(Res.string.theme_system_description)
        ThemeMode.Light -> stringResource(Res.string.theme_light_description)
        ThemeMode.Dark -> stringResource(Res.string.theme_dark_description)
    }
}
