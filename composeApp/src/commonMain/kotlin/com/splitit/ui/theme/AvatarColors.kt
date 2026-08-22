package com.splitit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Paleta de avatares de 8 colores (§3.4): variante light + dark por color.
@Immutable
data class AvatarColor(
    val name: String,
    val hex: String,
    val light: Color,
    val dark: Color,
    val onLight: Color = Color(0xFFFFFFFF),
    val onDark: Color = Color(0xFF221917),
)

val SplitItAvatarColors: List<AvatarColor> = listOf(
    AvatarColor("Coral", "#E0533D", Color(0xFFE0533D), Color(0xFFFF9E8C)),
    AvatarColor("Teal", "#0F7B7E", Color(0xFF0F7B7E), Color(0xFF6FD8DB)),
    AvatarColor("Índigo", "#5B5FC7", Color(0xFF5B5FC7), Color(0xFFA9ADFF)),
    AvatarColor("Ámbar", "#B26A00", Color(0xFFB26A00), Color(0xFFFFC66E)),
    AvatarColor("Frambuesa", "#B8375E", Color(0xFFB8375E), Color(0xFFFF8FAD)),
    AvatarColor("Verde", "#3E7C3A", Color(0xFF3E7C3A), Color(0xFF8FD18A)),
    AvatarColor("Océano", "#2E6DAE", Color(0xFF2E6DAE), Color(0xFF8FBDFF)),
    AvatarColor("Violeta", "#8A4FB8", Color(0xFF8A4FB8), Color(0xFFD3A4FF)),
)

val SplitItAvatarColorHexes: List<String> = SplitItAvatarColors.map { it.hex }

// Hex legacy persistidos → hex canónico de la nueva paleta (compatibilidad §3.4).
private val LegacyAvatarColorHexes = mapOf(
    "#2F80ED" to "#2E6DAE",
    "#27AE60" to "#3E7C3A",
    "#EB5757" to "#E0533D",
    "#F2994A" to "#B26A00",
    "#9B51E0" to "#8A4FB8",
    "#00A6A6" to "#0F7B7E",
)

fun avatarColorForHex(hex: String?): AvatarColor {
    val canonical = hex?.let { LegacyAvatarColorHexes[it] ?: it }
    return SplitItAvatarColors.firstOrNull { it.hex == canonical }
        ?: SplitItAvatarColors.first()
}

val LocalSplitItDarkTheme = staticCompositionLocalOf { false }
