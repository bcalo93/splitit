package com.splitit.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.splitit.domain.repository.ThemeMode
import kotlin.math.pow
import kotlin.math.round

@Suppress("DEPRECATION")
@org.jetbrains.compose.ui.tooling.preview.Preview(
    name = "Design Tokens",
    widthDp = 420,
    heightDp = 2400,
)
@Composable
fun TokensPreview() {
    SplitItTokenGallery()
}

@Composable
fun SplitItTokenGallery(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        TokenThemeSection(ThemeMode.Light, "Light")
        TokenThemeSection(ThemeMode.Dark, "Dark")
    }
}

@Composable
private fun TokenThemeSection(themeMode: ThemeMode, title: String) {
    SplitItTheme(themeMode = themeMode) {
        val colorScheme = MaterialTheme.colorScheme
        val semantic = LocalSplitItSemanticColors.current
        val spacing = LocalSplitItSpacing.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onBackground,
            )
            Spacer(Modifier.size(spacing.sm))

            SwatchRow("primary", colorScheme.primary, colorScheme.onPrimary)
            SwatchRow("primaryContainer", colorScheme.primaryContainer, colorScheme.onPrimaryContainer)
            SwatchRow("secondary", colorScheme.secondary, colorScheme.onSecondary)
            SwatchRow("secondaryContainer", colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)
            SwatchRow("tertiary", colorScheme.tertiary, colorScheme.onTertiary)
            SwatchRow("tertiaryContainer", colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
            SwatchRow("error", colorScheme.error, colorScheme.onError)
            SwatchRow("errorContainer", colorScheme.errorContainer, colorScheme.onErrorContainer)
            SwatchRow("background", colorScheme.background, colorScheme.onBackground)
            SwatchRow("surface", colorScheme.surface, colorScheme.onSurface)
            SwatchRow("surfaceVariant", colorScheme.surfaceVariant, colorScheme.onSurfaceVariant)

            Spacer(Modifier.size(spacing.md))
            Text(
                text = "Semánticos",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onBackground,
            )
            Spacer(Modifier.size(spacing.sm))

            SwatchRow("credit / surface", colorScheme.surface, semantic.credit)
            SwatchRow("debt / surface", colorScheme.surface, semantic.debt)
            SwatchRow("settled / surface", colorScheme.surface, semantic.settled)
            SwatchRow("staleWarning / surface", colorScheme.surface, semantic.staleWarning)
            SwatchRow(
                "surfaceCredit / onSecondaryContainer",
                semantic.surfaceCredit.over(colorScheme.surface),
                colorScheme.onSecondaryContainer,
            )
            SwatchRow(
                "surfaceDebt / onPrimaryContainer",
                semantic.surfaceDebt.over(colorScheme.surface),
                colorScheme.onPrimaryContainer,
            )

            Spacer(Modifier.size(spacing.md))
            Text(
                text = "Tipografía",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onBackground,
            )
            Spacer(Modifier.size(spacing.sm))

            TypographyRow("displaySmall", MaterialTheme.typography.displaySmall)
            TypographyRow("headlineMedium", MaterialTheme.typography.headlineMedium)
            TypographyRow("titleLarge", MaterialTheme.typography.titleLarge)
            TypographyRow("titleMedium", MaterialTheme.typography.titleMedium)
            TypographyRow("bodyLarge", MaterialTheme.typography.bodyLarge)
            TypographyRow("bodyMedium", MaterialTheme.typography.bodyMedium)
            TypographyRow("bodySmall", MaterialTheme.typography.bodySmall)
            TypographyRow("labelLarge", MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SwatchRow(
    name: String,
    container: Color,
    contentColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(SplitItRadius.extraSmall))
                .background(container),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Aa",
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = ratioLabel(contrastRatio(contentColor, container)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TypographyRow(name: String, style: TextStyle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = name,
            modifier = Modifier.width(140.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Cuentas claras 12,50 €",
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun ratioLabel(ratio: Double): String {
    val rounded = round(ratio * 100) / 100
    val verdict = if (ratio >= 4.5) "AA OK" else "AA FAIL"
    return "$rounded:1 · $verdict"
}

private fun Color.over(base: Color): Color {
    val a = alpha
    return Color(
        red = red * a + base.red * (1f - a),
        green = green * a + base.green * (1f - a),
        blue = blue * a + base.blue * (1f - a),
        alpha = 1f,
    )
}

private fun contrastRatio(foreground: Color, background: Color): Double {
    val fg = luminance(foreground)
    val bg = luminance(background)
    val lighter = maxOf(fg, bg)
    val darker = minOf(fg, bg)
    return (lighter + 0.05) / (darker + 0.05)
}

private fun luminance(color: Color): Double {
    fun channel(value: Float): Double {
        val v = value.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(color.red) +
        0.7152 * channel(color.green) +
        0.0722 * channel(color.blue)
}
