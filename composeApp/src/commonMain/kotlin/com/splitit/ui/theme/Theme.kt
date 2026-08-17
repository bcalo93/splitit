package com.splitit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.splitit.domain.repository.ThemeMode

private fun lightSplitItColorScheme() = lightColorScheme(
    primary = SplitItLightColors.primary,
    onPrimary = SplitItLightColors.onPrimary,
    primaryContainer = SplitItLightColors.primaryContainer,
    onPrimaryContainer = SplitItLightColors.onPrimaryContainer,
    inversePrimary = SplitItLightColors.inversePrimary,
    secondary = SplitItLightColors.secondary,
    onSecondary = SplitItLightColors.onSecondary,
    secondaryContainer = SplitItLightColors.secondaryContainer,
    onSecondaryContainer = SplitItLightColors.onSecondaryContainer,
    tertiary = SplitItLightColors.tertiary,
    onTertiary = SplitItLightColors.onTertiary,
    tertiaryContainer = SplitItLightColors.tertiaryContainer,
    onTertiaryContainer = SplitItLightColors.onTertiaryContainer,
    background = SplitItLightColors.background,
    onBackground = SplitItLightColors.onBackground,
    surface = SplitItLightColors.surface,
    onSurface = SplitItLightColors.onSurface,
    surfaceVariant = SplitItLightColors.surfaceVariant,
    onSurfaceVariant = SplitItLightColors.onSurfaceVariant,
    surfaceTint = SplitItLightColors.surfaceTint,
    inverseSurface = SplitItLightColors.inverseSurface,
    inverseOnSurface = SplitItLightColors.inverseOnSurface,
    error = SplitItLightColors.error,
    onError = SplitItLightColors.onError,
    errorContainer = SplitItLightColors.errorContainer,
    onErrorContainer = SplitItLightColors.onErrorContainer,
    outline = SplitItLightColors.outline,
    outlineVariant = SplitItLightColors.outlineVariant,
    scrim = SplitItLightColors.scrim,
    surfaceBright = SplitItLightColors.surfaceBright,
    surfaceDim = SplitItLightColors.surfaceDim,
    surfaceContainer = SplitItLightColors.surfaceContainer,
    surfaceContainerHigh = SplitItLightColors.surfaceContainerHigh,
    surfaceContainerHighest = SplitItLightColors.surfaceContainerHighest,
    surfaceContainerLow = SplitItLightColors.surfaceContainerLow,
    surfaceContainerLowest = SplitItLightColors.surfaceContainerLowest,
)

private fun darkSplitItColorScheme() = darkColorScheme(
    primary = SplitItDarkColors.primary,
    onPrimary = SplitItDarkColors.onPrimary,
    primaryContainer = SplitItDarkColors.primaryContainer,
    onPrimaryContainer = SplitItDarkColors.onPrimaryContainer,
    inversePrimary = SplitItDarkColors.inversePrimary,
    secondary = SplitItDarkColors.secondary,
    onSecondary = SplitItDarkColors.onSecondary,
    secondaryContainer = SplitItDarkColors.secondaryContainer,
    onSecondaryContainer = SplitItDarkColors.onSecondaryContainer,
    tertiary = SplitItDarkColors.tertiary,
    onTertiary = SplitItDarkColors.onTertiary,
    tertiaryContainer = SplitItDarkColors.tertiaryContainer,
    onTertiaryContainer = SplitItDarkColors.onTertiaryContainer,
    background = SplitItDarkColors.background,
    onBackground = SplitItDarkColors.onBackground,
    surface = SplitItDarkColors.surface,
    onSurface = SplitItDarkColors.onSurface,
    surfaceVariant = SplitItDarkColors.surfaceVariant,
    onSurfaceVariant = SplitItDarkColors.onSurfaceVariant,
    surfaceTint = SplitItDarkColors.surfaceTint,
    inverseSurface = SplitItDarkColors.inverseSurface,
    inverseOnSurface = SplitItDarkColors.inverseOnSurface,
    error = SplitItDarkColors.error,
    onError = SplitItDarkColors.onError,
    errorContainer = SplitItDarkColors.errorContainer,
    onErrorContainer = SplitItDarkColors.onErrorContainer,
    outline = SplitItDarkColors.outline,
    outlineVariant = SplitItDarkColors.outlineVariant,
    scrim = SplitItDarkColors.scrim,
    surfaceBright = SplitItDarkColors.surfaceBright,
    surfaceDim = SplitItDarkColors.surfaceDim,
    surfaceContainer = SplitItDarkColors.surfaceContainer,
    surfaceContainerHigh = SplitItDarkColors.surfaceContainerHigh,
    surfaceContainerHighest = SplitItDarkColors.surfaceContainerHighest,
    surfaceContainerLow = SplitItDarkColors.surfaceContainerLow,
    surfaceContainerLowest = SplitItDarkColors.surfaceContainerLowest,
)

@Composable
fun SplitItTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val colorScheme = if (useDarkTheme) darkSplitItColorScheme() else lightSplitItColorScheme()
    val semanticColors = if (useDarkTheme) DarkSplitItSemanticColors else LightSplitItSemanticColors

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = SplitItShapes,
        typography = SplitItTypography,
    ) {
        CompositionLocalProvider(
            LocalSplitItSemanticColors provides semanticColors,
            LocalSplitItMoneyStyles provides DefaultSplitItMoneyStyles,
            content = content,
        )
    }
}
