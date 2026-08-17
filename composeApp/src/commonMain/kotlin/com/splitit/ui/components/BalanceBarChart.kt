package com.splitit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.splitit.domain.value.Money
import com.splitit.ui.theme.LocalSplitItSemanticColors
import com.splitit.ui.theme.isReduceMotionEnabled
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.group_status_up_to_date
import kotlin.math.abs

private const val BarDurationMillis = 400
private const val BarStaggerMillis = 40
private const val FadeDurationMillis = 300

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
    val reduceMotion = isReduceMotionEnabled()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        entries.forEachIndexed { index, entry ->
            BalanceBarRow(
                entry = entry,
                maxAbs = maxAbs,
                index = index,
                reduceMotion = reduceMotion,
            )
        }
    }
}

@Composable
private fun BalanceBarRow(
    entry: BalanceBarEntry,
    maxAbs: Long,
    index: Int,
    reduceMotion: Boolean,
) {
    val semantic = LocalSplitItSemanticColors.current
    val minorUnits = entry.amount.minorUnits
    val fraction = if (minorUnits == 0L) {
        0f
    } else {
        (abs(minorUnits).toFloat() / maxAbs.toFloat()).coerceIn(0f, 1f)
    }

    val barProgress = remember { Animatable(0f) }
    val rowAlpha = remember { Animatable(if (reduceMotion) 0f else 1f) }

    LaunchedEffect(minorUnits, reduceMotion) {
        if (reduceMotion) {
            barProgress.snapTo(fraction)
            rowAlpha.snapTo(0f)
            rowAlpha.animateTo(1f, tween(FadeDurationMillis))
        } else {
            delay(index * BarStaggerMillis.toLong())
            barProgress.animateTo(
                targetValue = fraction,
                animationSpec = tween(BarDurationMillis, easing = FastOutSlowInEasing),
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(rowAlpha.value),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.weight(0.5f),
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
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(SplitItIcons.Check),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = semantic.settled,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.group_status_up_to_date),
                style = MaterialTheme.typography.bodyMedium,
                color = semantic.settled,
            )
        } else {
            val isCredit = minorUnits > 0
            val barColor = if (isCredit) semantic.credit else semantic.debt
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp),
            ) {
                val mid = size.width / 2f
                val barHalf = (size.width / 2f) * barProgress.value
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(
                        if (isCredit) SplitItIcons.ArrowDownward else SplitItIcons.ArrowUpward,
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = barColor,
                )
                Spacer(Modifier.width(4.dp))
                MoneyText(
                    amount = entry.amount,
                    variant = MoneyTextVariant.Caption,
                    tone = if (isCredit) MoneyTone.Credit else MoneyTone.Debit,
                    showSign = true,
                )
            }
        }
    }
}
