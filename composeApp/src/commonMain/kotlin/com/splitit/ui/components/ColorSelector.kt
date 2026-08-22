package com.splitit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.splitit.ui.theme.LocalSplitItDarkTheme
import com.splitit.ui.theme.SplitItAvatarColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.cd_check

@Composable
fun ColorSelector(
    selectedColor: String?,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalSplitItDarkTheme.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SplitItAvatarColors.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { avatar ->
                    val background = if (isDark) avatar.dark else avatar.light
                    val content = if (isDark) avatar.onDark else avatar.onLight
                    val isSelected = avatar.hex == selectedColor
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(background)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            )
                            .clickable { onColorSelected(avatar.hex) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(SplitItIcons.Check),
                                contentDescription = stringResource(Res.string.cd_check),
                                tint = content,
                            )
                        }
                    }
                }
            }
        }
    }
}
