package com.splitit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitit.ui.theme.LocalSplitItSemanticColors
import org.jetbrains.compose.resources.painterResource

enum class StatusChipStyle { UpToDate, Pending, Stale }

@Composable
fun StatusChip(
    style: StatusChipStyle,
    label: String,
    modifier: Modifier = Modifier,
) {
    val semantic = LocalSplitItSemanticColors.current
    val colorScheme = MaterialTheme.colorScheme
    val (container, content, icon) = when (style) {
        StatusChipStyle.UpToDate -> Triple(
            colorScheme.secondaryContainer,
            colorScheme.onSecondaryContainer,
            SplitItIcons.Check,
        )
        StatusChipStyle.Pending -> Triple(
            colorScheme.tertiaryContainer,
            colorScheme.onTertiaryContainer,
            SplitItIcons.WarningAmber,
        )
        StatusChipStyle.Stale -> Triple(
            colorScheme.tertiaryContainer,
            semantic.staleWarning,
            SplitItIcons.WarningAmber,
        )
    }

    Surface(
        modifier = modifier,
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = content,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}
