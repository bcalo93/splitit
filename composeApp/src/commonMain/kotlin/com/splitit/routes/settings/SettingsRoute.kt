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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                actions = {
                    Button(
                        modifier = Modifier.padding(end = 16.dp),
                        enabled = !state.isLoading && !state.isSaving,
                        onClick = onSave,
                    ) {
                        Text(if (state.isSaving) "Saving" else "Save")
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
                    text = "Preferences are stored only on this device.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedTextField(
                    value = state.draftSettings.defaultCurrencyCode,
                    onValueChange = onCurrencyCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving,
                    label = { Text("Default currency") },
                    supportingText = {
                        if (state.currencyError != null) {
                            Text(state.currencyError)
                        } else {
                            Text("Use a 3-letter ISO code for new expenses, such as USD or EUR.")
                        }
                    },
                    isError = state.currencyError != null,
                    singleLine = true,
                )
                Text(
                    text = "Theme",
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
                        text = "Settings saved locally.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (state.errorMessage != null) {
                    OutlinedButton(onClick = onRetry) {
                        Text("Retry")
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

private fun themeModeLabel(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.System -> "System default"
        ThemeMode.Light -> "Light"
        ThemeMode.Dark -> "Dark"
    }
}

private fun themeModeDescription(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.System -> "Follow the device theme."
        ThemeMode.Light -> "Always use the light theme."
        ThemeMode.Dark -> "Always use the dark theme."
    }
}
