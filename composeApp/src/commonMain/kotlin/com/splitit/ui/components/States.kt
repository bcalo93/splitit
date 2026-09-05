package com.splitit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.clear_search
import splitit.composeapp.generated.resources.retry
import com.splitit.ui.theme.LocalSplitItSpacing

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
) {
    SkeletonList(
        itemCount = 3,
        modifier = modifier,
    )
}

@Composable
fun InlineErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSplitItSpacing.current
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(SplitItIcons.WarningAmber),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(Res.string.retry))
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = message,
        icon = SplitItIcons.WarningAmber,
        ctaText = stringResource(Res.string.retry),
        onCtaClick = onRetry,
        modifier = modifier,
    )
}

@Composable
fun NoSearchResultsState(
    title: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSplitItSpacing.current
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(
            title = title,
            icon = SplitItIcons.SearchOff,
            ctaText = stringResource(Res.string.clear_search),
            onCtaClick = onClear,
            modifier = Modifier.padding(horizontal = spacing.xl),
        )
    }
}
