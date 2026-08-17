package com.splitit.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import splitit.composeapp.generated.resources.no_results_match
import splitit.composeapp.generated.resources.retry

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
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
    query: String,
    entityName: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = stringResource(Res.string.no_results_match, entityName, query),
        icon = SplitItIcons.SearchOff,
        ctaText = stringResource(Res.string.clear_search),
        onCtaClick = onClear,
        modifier = modifier,
    )
}
