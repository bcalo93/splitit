package com.splitit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Paleta de marca — Light (§3.1)
internal object SplitItLightColors {
    val primary = Color(0xFFC43C2E)
    val onPrimary = Color(0xFFFFFFFF)
    val primaryContainer = Color(0xFFFFDAD3)
    val onPrimaryContainer = Color(0xFF410100)
    val inversePrimary = Color(0xFFFFB4A3)
    val secondary = Color(0xFF00696B)
    val onSecondary = Color(0xFFFFFFFF)
    val secondaryContainer = Color(0xFF9CF1F0)
    val onSecondaryContainer = Color(0xFF002020)
    val tertiary = Color(0xFF7C5700)
    val onTertiary = Color(0xFFFFFFFF)
    val tertiaryContainer = Color(0xFFFFDEA6)
    val onTertiaryContainer = Color(0xFF271900)
    val background = Color(0xFFFFF8F6)
    val onBackground = Color(0xFF221917)
    val surface = Color(0xFFFFF8F6)
    val onSurface = Color(0xFF221917)
    val surfaceVariant = Color(0xFFF4DDD7)
    val onSurfaceVariant = Color(0xFF524342)
    val surfaceTint = Color(0xFFC43C2E)
    val inverseSurface = Color(0xFF382E2C)
    val inverseOnSurface = Color(0xFFFFEDE9)
    val error = Color(0xFFBA1A1A)
    val onError = Color(0xFFFFFFFF)
    val errorContainer = Color(0xFFFFDAD6)
    val onErrorContainer = Color(0xFF410001)
    val outline = Color(0xFF857371)
    val outlineVariant = Color(0xFFD8C2BD)
    val scrim = Color(0xFF000000)
    val surfaceBright = Color(0xFFFFFFFF)
    val surfaceDim = Color(0xFFE4D5D0)
    val surfaceContainer = Color(0xFFF6EAE6)
    val surfaceContainerHigh = Color(0xFFF1E3DF)
    val surfaceContainerHighest = Color(0xFFEBDCD8)
    val surfaceContainerLow = Color(0xFFFBF1EE)
    val surfaceContainerLowest = Color(0xFFFFFFFF)
}

// Paleta de marca — Dark (§3.2)
internal object SplitItDarkColors {
    val primary = Color(0xFFFFB4A3)
    val onPrimary = Color(0xFF5F1609)
    val primaryContainer = Color(0xFF992515)
    val onPrimaryContainer = Color(0xFFFFDAD3)
    val inversePrimary = Color(0xFFC43C2E)
    val secondary = Color(0xFF4CDADB)
    val onSecondary = Color(0xFF003738)
    val secondaryContainer = Color(0xFF004F51)
    val onSecondaryContainer = Color(0xFF9CF1F0)
    val tertiary = Color(0xFFF2BE4D)
    val onTertiary = Color(0xFF3F2E00)
    val tertiaryContainer = Color(0xFF5B4300)
    val onTertiaryContainer = Color(0xFFFFDEA6)
    val background = Color(0xFF1A1110)
    val onBackground = Color(0xFFF1DFDB)
    val surface = Color(0xFF1A1110)
    val onSurface = Color(0xFFF1DFDB)
    val surfaceVariant = Color(0xFF534341)
    val onSurfaceVariant = Color(0xFFD8C2BD)
    val surfaceTint = Color(0xFFFFB4A3)
    val inverseSurface = Color(0xFFF1DFDB)
    val inverseOnSurface = Color(0xFF382E2C)
    val error = Color(0xFFFFB4AB)
    val onError = Color(0xFF690005)
    val errorContainer = Color(0xFF93000A)
    val onErrorContainer = Color(0xFFFFDAD6)
    val outline = Color(0xFFA08C88)
    val outlineVariant = Color(0xFF534341)
    val scrim = Color(0xFF000000)
    val surfaceBright = Color(0xFF3A2B27)
    val surfaceDim = Color(0xFF140C0B)
    val surfaceContainer = Color(0xFF271C1A)
    val surfaceContainerHigh = Color(0xFF2D211F)
    val surfaceContainerHighest = Color(0xFF342724)
    val surfaceContainerLow = Color(0xFF211715)
    val surfaceContainerLowest = Color(0xFF140C0B)
}

// Colores semánticos de dominio (§3.3)
@Immutable
data class SplitItSemanticColors(
    val credit: Color,
    val debt: Color,
    val settled: Color,
    val staleWarning: Color,
    val surfaceCredit: Color,
    val surfaceDebt: Color,
)

internal val LightSplitItSemanticColors = SplitItSemanticColors(
    credit = Color(0xFF00696B),
    debt = Color(0xFFC43C2E),
    settled = Color(0xFF524342),
    staleWarning = Color(0xFF7C5700),
    surfaceCredit = Color(0xFF9CF1F0).copy(alpha = 0.40f),
    surfaceDebt = Color(0xFFFFDAD3),
)

internal val DarkSplitItSemanticColors = SplitItSemanticColors(
    credit = Color(0xFF4CDADB),
    debt = Color(0xFFFFB4A3),
    settled = Color(0xFFD8C2BD),
    staleWarning = Color(0xFFF2BE4D),
    surfaceCredit = Color(0xFF004F51),
    surfaceDebt = Color(0xFF992515).copy(alpha = 0.60f),
)

val LocalSplitItSemanticColors = staticCompositionLocalOf { LightSplitItSemanticColors }
