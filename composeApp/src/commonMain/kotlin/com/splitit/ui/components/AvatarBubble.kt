package com.splitit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.splitit.ui.theme.LocalSplitItDarkTheme
import com.splitit.ui.theme.avatarColorForHex

@Immutable
data class AvatarStackItem(
    val name: String,
    val colorHex: String?,
)

@Composable
fun AvatarBubble(
    name: String,
    colorHex: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Unspecified,
) {
    val avatar = avatarColorForHex(colorHex)
    val isDark = LocalSplitItDarkTheme.current
    val background = if (isDark) avatar.dark else avatar.light
    val content = if (isDark) avatar.onDark else avatar.onLight
    val textStyle = when {
        size >= 40.dp -> MaterialTheme.typography.titleMedium
        size >= 28.dp -> MaterialTheme.typography.labelLarge
        else -> MaterialTheme.typography.bodySmall
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(
                        width = borderWidth,
                        color = if (borderColor == Color.Unspecified) MaterialTheme.colorScheme.surface else borderColor,
                        shape = CircleShape,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials(name),
            style = textStyle,
            color = content,
        )
    }
}

@Composable
fun AvatarStack(
    items: List<AvatarStackItem>,
    modifier: Modifier = Modifier,
    maxVisible: Int = 3,
    size: Dp = 28.dp,
    overlap: Dp = 8.dp,
) {
    val visible = items.take(maxVisible)
    val extra = items.size - visible.size
    val count = visible.size + if (extra > 0) 1 else 0
    if (count == 0) return

    val totalWidth = size + (size - overlap) * (count - 1)

    Box(
        modifier = modifier
            .requiredWidth(totalWidth)
            .height(size),
    ) {
        visible.forEachIndexed { index, item ->
            AvatarBubble(
                name = item.name,
                colorHex = item.colorHex,
                size = size,
                borderWidth = 2.dp,
                modifier = Modifier.offset(x = (size - overlap) * index),
            )
        }
        if (extra > 0) {
            OverflowAvatarBubble(
                count = extra,
                size = size,
                modifier = Modifier.offset(x = (size - overlap) * visible.size),
            )
        }
    }
}

@Composable
private fun OverflowAvatarBubble(
    count: Int,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+$count",
            style = if (size >= 40.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun initials(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val first = words.firstOrNull()?.firstOrNull()?.uppercaseChar()?.toString() ?: ""
    val second = words.getOrNull(1)?.firstOrNull()?.uppercaseChar()?.toString() ?: ""
    return first + second
}
