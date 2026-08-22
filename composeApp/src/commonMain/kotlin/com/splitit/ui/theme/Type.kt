package com.splitit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Escala tipográfica M3 ajustada (§4.1), fuentes del sistema.
val SplitItTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)

// Estilos semánticos de dinero con cifras tabulares (§4.2)
@Immutable
data class SplitItMoneyStyles(
    val moneyHero: TextStyle,
    val moneyRow: TextStyle,
    val moneyCaption: TextStyle,
)

internal val DefaultSplitItMoneyStyles = SplitItMoneyStyles(
    moneyHero = SplitItTypography.displaySmall.copy(fontFeatureSettings = "tnum"),
    moneyRow = SplitItTypography.titleMedium.copy(fontFeatureSettings = "tnum"),
    moneyCaption = SplitItTypography.bodyMedium.copy(fontFeatureSettings = "tnum"),
)

val LocalSplitItMoneyStyles = staticCompositionLocalOf { DefaultSplitItMoneyStyles }
