package com.splitit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.clear
import splitit.composeapp.generated.resources.clear_search
import splitit.composeapp.generated.resources.loading
import splitit.composeapp.generated.resources.no_results_match
import splitit.composeapp.generated.resources.retry

@Composable
fun SearchField(
    query: String,
    label: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        trailingIcon = if (query.isNotEmpty()) {
            {
                TextButton(onClick = { onQueryChange("") }) {
                    Text(stringResource(Res.string.clear))
                }
            }
        } else {
            null
        },
    )
}

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(Res.string.loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun InlineErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        OutlinedButton(onClick = onRetry) {
            Text(stringResource(Res.string.retry))
        }
    }
}

@Composable
fun NoSearchResultsState(
    query: String,
    entityName: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.no_results_match, entityName, query),
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedButton(onClick = onClear) {
            Text(stringResource(Res.string.clear_search))
        }
    }
}

fun participantColor(color: String?): Color {
    return when (color) {
        "#2F80ED" -> Color(0xFF2F80ED)
        "#27AE60" -> Color(0xFF27AE60)
        "#EB5757" -> Color(0xFFEB5757)
        "#F2994A" -> Color(0xFFF2994A)
        "#9B51E0" -> Color(0xFF9B51E0)
        "#00A6A6" -> Color(0xFF00A6A6)
        else -> Color(0xFF2F80ED)
    }
}
