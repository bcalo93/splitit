package com.splitit.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.cd_clear
import splitit.composeapp.generated.resources.cd_search

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: DrawableResource? = null,
    error: String? = null,
    supportingText: String? = null,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String? = null,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    shape: Shape = MaterialTheme.shapes.small,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = shape,
        isError = error != null,
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        supportingText = when {
            error != null -> ({ Text(error) })
            supportingText != null -> ({ Text(supportingText) })
            maxLength != null -> ({ Text("${value.length} / $maxLength") })
            else -> null
        },
        keyboardOptions = keyboardOptions,
    )
}

@Composable
fun SearchField(
    query: String,
    label: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        shape = CircleShape,
        leadingIcon = {
            Icon(
                painter = painterResource(SplitItIcons.Search),
                contentDescription = stringResource(Res.string.cd_search),
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(SplitItIcons.Close),
                        contentDescription = stringResource(Res.string.cd_clear),
                    )
                }
            }
        } else {
            null
        },
    )
}
