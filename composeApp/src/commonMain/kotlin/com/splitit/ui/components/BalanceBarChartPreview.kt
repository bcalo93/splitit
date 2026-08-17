package com.splitit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitit.domain.repository.ThemeMode
import com.splitit.domain.value.Money
import com.splitit.ui.theme.SplitItTheme

private val allZeroEntries = listOf(
    BalanceBarEntry("Ana", "#E0533D", Money(0, "EUR")),
    BalanceBarEntry("Luis", "#0F7B7E", Money(0, "EUR")),
    BalanceBarEntry("Bea", "#5B5FC7", Money(0, "EUR")),
)

private val oneBigDebtorEntries = listOf(
    BalanceBarEntry("Ana", "#E0533D", Money(-32000, "EUR")),
    BalanceBarEntry("Luis", "#0F7B7E", Money(8000, "EUR")),
    BalanceBarEntry("Bea", "#5B5FC7", Money(12000, "EUR")),
    BalanceBarEntry("Carlos", "#B26A00", Money(12000, "EUR")),
)

private val manyParticipantsEntries = listOf(
    BalanceBarEntry("Ana", "#E0533D", Money(12000, "EUR")),
    BalanceBarEntry("Luis", "#0F7B7E", Money(-4500, "EUR")),
    BalanceBarEntry("Bea", "#5B5FC7", Money(0, "EUR")),
    BalanceBarEntry("Carlos", "#B26A00", Money(-8200, "EUR")),
    BalanceBarEntry("Diana", "#B8375E", Money(2300, "EUR")),
    BalanceBarEntry("Elena", "#3E7C3A", Money(-1600, "EUR")),
)

@Composable
private fun BalanceChartPreview(entries: List<BalanceBarEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        BalanceBarChart(entries = entries)
    }
}

@Suppress("DEPRECATION")
@org.jetbrains.compose.ui.tooling.preview.Preview(name = "Balances — all zero — Light", widthDp = 420)
@Composable
fun BalanceChartAllZeroLightPreview() {
    SplitItTheme(ThemeMode.Light) { BalanceChartPreview(allZeroEntries) }
}

@Suppress("DEPRECATION")
@org.jetbrains.compose.ui.tooling.preview.Preview(name = "Balances — all zero — Dark", widthDp = 420)
@Composable
fun BalanceChartAllZeroDarkPreview() {
    SplitItTheme(ThemeMode.Dark) { BalanceChartPreview(allZeroEntries) }
}

@Suppress("DEPRECATION")
@org.jetbrains.compose.ui.tooling.preview.Preview(name = "Balances — one big debtor — Light", widthDp = 420)
@Composable
fun BalanceChartOneBigDebtorLightPreview() {
    SplitItTheme(ThemeMode.Light) { BalanceChartPreview(oneBigDebtorEntries) }
}

@Suppress("DEPRECATION")
@org.jetbrains.compose.ui.tooling.preview.Preview(name = "Balances — one big debtor — Dark", widthDp = 420)
@Composable
fun BalanceChartOneBigDebtorDarkPreview() {
    SplitItTheme(ThemeMode.Dark) { BalanceChartPreview(oneBigDebtorEntries) }
}

@Suppress("DEPRECATION")
@org.jetbrains.compose.ui.tooling.preview.Preview(name = "Balances — N participants — Light", widthDp = 420)
@Composable
fun BalanceChartManyParticipantsLightPreview() {
    SplitItTheme(ThemeMode.Light) { BalanceChartPreview(manyParticipantsEntries) }
}

@Suppress("DEPRECATION")
@org.jetbrains.compose.ui.tooling.preview.Preview(name = "Balances — N participants — Dark", widthDp = 420)
@Composable
fun BalanceChartManyParticipantsDarkPreview() {
    SplitItTheme(ThemeMode.Dark) { BalanceChartPreview(manyParticipantsEntries) }
}
