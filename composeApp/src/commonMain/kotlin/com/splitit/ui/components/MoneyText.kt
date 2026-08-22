package com.splitit.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.splitit.domain.value.Money
import com.splitit.ui.theme.LocalSplitItMoneyStyles
import com.splitit.ui.theme.LocalSplitItSemanticColors

enum class MoneyTextVariant { Hero, Row, Caption }

enum class MoneyTone { Credit, Debit, Settled }

@Composable
fun MoneyText(
    amount: Money,
    variant: MoneyTextVariant = MoneyTextVariant.Row,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    tone: MoneyTone? = null,
    showSign: Boolean = false,
    showCurrency: Boolean = false,
) {
    val moneyStyles = LocalSplitItMoneyStyles.current
    val semantic = LocalSplitItSemanticColors.current
    val style = when (variant) {
        MoneyTextVariant.Hero -> moneyStyles.moneyHero
        MoneyTextVariant.Row -> moneyStyles.moneyRow
        MoneyTextVariant.Caption -> moneyStyles.moneyCaption
    }
    val resolvedColor = when {
        tone == MoneyTone.Credit -> semantic.credit
        tone == MoneyTone.Debit -> semantic.debt
        tone == MoneyTone.Settled -> semantic.settled
        color != Color.Unspecified -> color
        else -> MaterialTheme.colorScheme.onSurface
    }

    Text(
        modifier = modifier,
        text = formatMoney(amount, showSign = showSign, showCurrency = showCurrency),
        style = style,
        color = resolvedColor,
    )
}

fun formatMoney(
    amount: Money,
    showSign: Boolean = false,
    showCurrency: Boolean = false,
): String {
    val abs = if (amount.minorUnits < 0) -amount.minorUnits else amount.minorUnits
    val major = abs / 100
    val minor = abs % 100
    val number = if (minor == 0L) major.toString() else "$major.${minor.toString().padStart(2, '0')}"
    val sign = when {
        !showSign -> ""
        amount.minorUnits > 0 -> "+"
        amount.minorUnits < 0 -> "-"
        else -> ""
    }
    return if (showCurrency) "$sign$number ${amount.currencyCode}" else "$sign$number"
}
