package com.splitit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.localization.DefaultLocalizationService
import com.splitit.presentation.settings.SettingsViewModel
import com.splitit.routes.SplitItRoutes
import com.splitit.ui.theme.SplitItTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val localizationService: DefaultLocalizationService = koinInject()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        localizationService.initialize()
    }

    SplitItTheme(themeMode = settingsState.settings.themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            SplitItRoutes(settingsViewModel = settingsViewModel)
        }
    }
}
