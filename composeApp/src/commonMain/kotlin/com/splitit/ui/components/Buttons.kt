package com.splitit.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: DrawableResource? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = CircleShape,
    ) {
        ButtonContent(
            text = text,
            isLoading = isLoading,
            icon = icon,
            iconTint = MaterialTheme.colorScheme.onPrimary,
            progressColor = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: DrawableResource? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = CircleShape,
    ) {
        ButtonContent(
            text = text,
            isLoading = isLoading,
            icon = icon,
            iconTint = MaterialTheme.colorScheme.primary,
            progressColor = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    icon: DrawableResource?,
    iconTint: androidx.compose.ui.graphics.Color,
    progressColor: androidx.compose.ui.graphics.Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = progressColor,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = iconTint,
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}
