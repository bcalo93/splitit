package com.splitit.routes.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.appVersion
import com.splitit.domain.repository.ThemeMode
import com.splitit.presentation.settings.SettingsUiState
import com.splitit.presentation.settings.SettingsViewModel
import com.splitit.ui.components.InlineErrorState
import com.splitit.ui.components.SplitItIcons
import com.splitit.ui.components.SplitItScaffold
import com.splitit.ui.components.SplitItTopBar
import com.splitit.ui.theme.LocalSplitItSpacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.about_version_label
import splitit.composeapp.generated.resources.cancel
import splitit.composeapp.generated.resources.currency_common
import splitit.composeapp.generated.resources.currency_custom_label
import splitit.composeapp.generated.resources.default_currency
import splitit.composeapp.generated.resources.error_invalid_currency
import splitit.composeapp.generated.resources.preferences_stored_locally
import splitit.composeapp.generated.resources.save
import splitit.composeapp.generated.resources.section_about
import splitit.composeapp.generated.resources.section_appearance
import splitit.composeapp.generated.resources.section_general
import splitit.composeapp.generated.resources.settings
import splitit.composeapp.generated.resources.settings_saved
import splitit.composeapp.generated.resources.theme_dark
import splitit.composeapp.generated.resources.theme_light
import splitit.composeapp.generated.resources.theme_system

private val COMMON_CURRENCY_CODES = listOf(
    "USD", "EUR", "GBP", "JPY", "CNY", "AUD", "CAD", "CHF",
    "MXN", "ARS", "BRL", "CLP", "COP", "UYU", "PEN",
)

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val settingsSavedMessage = stringResource(Res.string.settings_saved)

    LaunchedEffect(state.saveCompleted) {
        if (state.saveCompleted) {
            viewModel.consumeSaveCompleted()
            snackbarHostState.showSnackbar(settingsSavedMessage)
        }
    }

    SettingsScreen(
        state = state,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        onCurrencySelected = { code ->
            viewModel.onCurrencyCodeChange(code)
            viewModel.save()
        },
        onThemeModeSelected = { mode ->
            viewModel.onThemeModeSelected(mode)
            viewModel.save()
        },
        onRetry = viewModel::refresh,
    )
}

@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onCurrencySelected: (String) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onRetry: () -> Unit,
) {
    val spacing = LocalSplitItSpacing.current
    var showCurrencyDialog by remember { mutableStateOf(false) }

    SplitItScaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            SplitItTopBar(
                title = stringResource(Res.string.settings),
                onBack = onBack,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                    .verticalScroll(rememberScrollState()),
            ) {
                state.errorMessage?.let { message ->
                    InlineErrorState(message = message, onRetry = onRetry)
                }

                SectionHeader(stringResource(Res.string.section_general))
                PreferenceRow(
                    title = stringResource(Res.string.default_currency),
                    value = state.settings.defaultCurrencyCode,
                    onClick = { showCurrencyDialog = true },
                )

                SectionHeader(stringResource(Res.string.section_appearance))
                ThemeSegmentedButtons(
                    selected = state.draftSettings.themeMode,
                    enabled = !state.isSaving,
                    onSelect = onThemeModeSelected,
                )

                SectionHeader(stringResource(Res.string.section_about))
                PreferenceRow(
                    title = stringResource(Res.string.about_version_label),
                    value = appVersion(),
                )

                Text(
                    text = stringResource(Res.string.preferences_stored_locally),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = spacing.md, vertical = 16.dp),
                )
            }
        }
    }

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            currentCode = state.settings.defaultCurrencyCode,
            onDismiss = { showCurrencyDialog = false },
            onConfirm = { code ->
                showCurrencyDialog = false
                onCurrencySelected(code)
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    val spacing = LocalSplitItSpacing.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = spacing.md, end = spacing.md, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun PreferenceRow(
    title: String,
    value: String?,
    onClick: (() -> Unit)? = null,
) {
    val spacing = LocalSplitItSpacing.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            ),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onClick != null) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(SplitItIcons.ChevronRight),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSegmentedButtons(
    selected: ThemeMode,
    enabled: Boolean,
    onSelect: (ThemeMode) -> Unit,
) {
    val spacing = LocalSplitItSpacing.current
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md),
    ) {
        ThemeMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                icon = {
                    Icon(
                        painter = painterResource(themeModeIcon(mode)),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            ) {
                Text(themeModeLabel(mode))
            }
        }
    }
}

@Composable
private fun themeModeIcon(themeMode: ThemeMode): DrawableResource = when (themeMode) {
    ThemeMode.System -> SplitItIcons.Contrast
    ThemeMode.Light -> SplitItIcons.LightMode
    ThemeMode.Dark -> SplitItIcons.DarkMode
}

@Composable
private fun themeModeLabel(themeMode: ThemeMode): String = when (themeMode) {
    ThemeMode.System -> stringResource(Res.string.theme_system)
    ThemeMode.Light -> stringResource(Res.string.theme_light)
    ThemeMode.Dark -> stringResource(Res.string.theme_dark)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CurrencyPickerDialog(
    currentCode: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var draft by remember { mutableStateOf(currentCode) }
    val normalized = draft.trim().uppercase()
    val isValid = normalized.length == 3 && normalized.all { it in 'A'..'Z' }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text(stringResource(Res.string.default_currency)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(Res.string.currency_common),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    COMMON_CURRENCY_CODES.forEach { code ->
                        FilterChip(
                            selected = code == normalized,
                            onClick = { draft = code },
                            label = { Text(code) },
                        )
                    }
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.take(3).uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.currency_custom_label)) },
                    supportingText = {
                        if (!isValid) {
                            Text(stringResource(Res.string.error_invalid_currency))
                        }
                    },
                    isError = !isValid,
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(normalized) },
                enabled = isValid,
            ) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}
