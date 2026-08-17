package com.splitit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.splitit.domain.value.Money
import com.splitit.ui.theme.LocalSplitItSemanticColors
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs

@Immutable
data class BalanceBarEntry(
    val name: String,
    val colorHex: String?,
    val amount: Money,
)

@Composable
fun BalanceBarChart(
    entries: List<BalanceBarEntry>,
    modifier: Modifier = Modifier,
) {
    val maxAbs = entries.map { abs(it.amount.minorUnits) }.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        entries.forEach { entry ->
            BalanceBarRow(entry = entry, maxAbs = maxAbs)
        }
    }
}

@Composable
private fun BalanceBarRow(
    entry: BalanceBarEntry,
    maxAbs: Long,
) {
    val semantic = LocalSplitItSemanticColors.current
    val minorUnits = entry.amount.minorUnits
    val axisColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.weight(0.6f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarBubble(
                name = entry.name,
                colorHex = entry.colorHex,
                size = 28.dp,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (minorUnits == 0L) {
            Icon(
                painter = painterResource(SplitItIcons.Check),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = semantic.settled,
            )
        } else {
            val isCredit = minorUnits > 0
            val fraction = abs(minorUnits).toFloat() / maxAbs.toFloat()
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp),
            ) {
                val mid = size.width / 2f
                drawLine(
                    color = axisColor,
                    start = Offset(mid, 0f),
                    end = Offset(mid, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
                val barHalf = (size.width / 2f) * fraction.coerceIn(0f, 1f)
                val barColor = if (isCredit) semantic.credit else semantic.debt
                val corner = CornerRadius(size.height / 2f)
                if (barHalf > 0f) {
                    val left = if (isCredit) mid else mid - barHalf
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, 0f),
                        size = Size(barHalf, size.height),
                        cornerRadius = corner,
                    )
                }
            }
        }

        MoneyText(
            amount = entry.amount,
            variant = MoneyTextVariant.Caption,
            tone = when {
                minorUnits > 0 -> MoneyTone.Credit
                minorUnits < 0 -> MoneyTone.Debit
                else -> MoneyTone.Settled
            },
            showSign = minorUnits != 0L,
        )
    }
}
