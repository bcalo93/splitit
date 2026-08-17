package com.splitit.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.splitit.domain.value.Money
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.cd_delete
import splitit.composeapp.generated.resources.cd_edit
import splitit.composeapp.generated.resources.cd_more_vert
import splitit.composeapp.generated.resources.transfer_content_description

@Composable
fun GroupCard(
    title: String,
    subtitle: String,
    avatarItems: List<AvatarStackItem>,
    status: StatusChipStyle,
    statusLabel: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onMoreClick: (() -> Unit)? = null,
) {
    SplitItCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarStack(items = avatarItems, size = 28.dp, overlap = 8.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(style = status, label = statusLabel)
            if (onMoreClick != null) {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        painter = painterResource(SplitItIcons.MoreVert),
                        contentDescription = stringResource(Res.string.cd_more_vert),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(
    title: String,
    payerName: String,
    payerColorHex: String?,
    metadata: String,
    amount: Money,
    modifier: Modifier = Modifier,
    note: String? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    SplitItCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarBubble(
                    name = payerName,
                    colorHex = payerColorHex,
                    size = 40.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = metadata,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(8.dp))
                MoneyText(
                    amount = amount,
                    variant = MoneyTextVariant.Row,
                    showCurrency = true,
                )
                if (onMoreClick != null) {
                    IconButton(onClick = onMoreClick) {
                        Icon(
                            painter = painterResource(SplitItIcons.MoreVert),
                            contentDescription = stringResource(Res.string.cd_more_vert),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ParticipantRow(
    name: String,
    colorHex: String?,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    SplitItCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarBubble(
                name = name,
                colorHex = colorHex,
                size = 48.dp,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (onMoreClick != null) {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        painter = painterResource(SplitItIcons.MoreVert),
                        contentDescription = stringResource(Res.string.cd_more_vert),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun TransferCard(
    fromName: String,
    fromColorHex: String?,
    toName: String,
    toColorHex: String?,
    amount: Money,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(
        Res.string.transfer_content_description,
        fromName,
        toName,
        formatMoney(amount, showCurrency = true),
    )
    SplitItCard(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = description
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TransferEndpoint(name = fromName, colorHex = fromColorHex, modifier = Modifier.weight(1f))
                TransferConnector()
                TransferEndpoint(name = toName, colorHex = toColorHex, modifier = Modifier.weight(1f))
            }
            MoneyText(
                amount = amount,
                variant = MoneyTextVariant.Row,
                showCurrency = true,
            )
        }
    }
}

@Composable
private fun TransferEndpoint(
    name: String,
    colorHex: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AvatarBubble(name = name, colorHex = colorHex, size = 40.dp)
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TransferConnector() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Icon(
            painter = painterResource(SplitItIcons.SwapHoriz),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun SplitItCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = content,
    )
}
