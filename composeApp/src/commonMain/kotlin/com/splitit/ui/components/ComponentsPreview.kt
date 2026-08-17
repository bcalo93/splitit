package com.splitit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitit.domain.repository.ThemeMode
import com.splitit.domain.value.Money
import com.splitit.ui.theme.SplitItTheme
import org.jetbrains.compose.resources.stringResource
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.delete

@Suppress("DEPRECATION")
@org.jetbrains.compose.ui.tooling.preview.Preview(
    name = "Component Gallery — Light",
    widthDp = 420,
    heightDp = 3200,
)
@Composable
fun ComponentGalleryLightPreview() {
    SplitItTheme(ThemeMode.Light) { ComponentGallery() }
}

@Suppress("DEPRECATION")
@org.jetbrains.compose.ui.tooling.preview.Preview(
    name = "Component Gallery — Dark",
    widthDp = 420,
    heightDp = 3200,
)
@Composable
fun ComponentGalleryDarkPreview() {
    SplitItTheme(ThemeMode.Dark) { ComponentGallery() }
}

@Composable
private fun ComponentGallery() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GallerySection("AvatarBubble (24/28/40/48)") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AvatarBubble("Ana", "#E0533D", 24.dp)
                AvatarBubble("Luis", "#0F7B7E", 28.dp)
                AvatarBubble("Bea", "#5B5FC7", 40.dp)
                AvatarBubble("María José", "#B26A00", 48.dp)
            }
        }

        GallerySection("AvatarStack") {
            AvatarStack(
                items = listOf(
                    AvatarStackItem("Ana", "#E0533D"),
                    AvatarStackItem("Luis", "#0F7B7E"),
                    AvatarStackItem("Bea", "#5B5FC7"),
                    AvatarStackItem("Carlos", "#B26A00"),
                    AvatarStackItem("Diana", "#B8375E"),
                ),
            )
        }

        GallerySection("MoneyText") {
            MoneyText(Money(123456, "EUR"), MoneyTextVariant.Hero, showCurrency = true)
            MoneyText(Money(-3000, "EUR"), MoneyTextVariant.Row, tone = MoneyTone.Debit, showSign = true)
            MoneyText(Money(1250, "EUR"), MoneyTextVariant.Caption, tone = MoneyTone.Credit, showSign = true)
            MoneyText(Money(0, "EUR"), MoneyTextVariant.Caption, tone = MoneyTone.Settled)
        }

        GallerySection("StatusChip") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(StatusChipStyle.UpToDate, "Al día")
                StatusChip(StatusChipStyle.Pending, "Pendiente")
                StatusChip(StatusChipStyle.Stale, "Stale")
            }
        }

        GallerySection("Buttons") {
            PrimaryButton("Crear grupo", onClick = {}, icon = SplitItIcons.Add)
            PrimaryButton("Guardando", onClick = {}, isLoading = true)
            SecondaryButton("Cancelar", onClick = {})
        }

        GallerySection("FormTextField / SearchField") {
            FormTextField(
                value = "",
                onValueChange = {},
                label = "Nombre",
                leadingIcon = SplitItIcons.Group,
                error = "Introduce un nombre",
                maxLength = 40,
            )
            SearchField(query = "cena", label = "Buscar", onQueryChange = {})
        }

        GallerySection("ColorSelector") {
            ColorSelector(selectedColor = "#E0533D", onColorSelected = {})
        }

        GallerySection("ShareWeightStepper") {
            var parts by remember { mutableIntStateOf(2) }
            ShareWeightStepper(
                value = parts,
                onValueChange = { parts = it },
                resultAmount = Money(parts * 625L, "EUR"),
            )
        }

        GallerySection("BalanceBarChart") {
            BalanceBarChart(
                entries = listOf(
                    BalanceBarEntry("Ana", "#E0533D", Money(12000, "EUR")),
                    BalanceBarEntry("Luis", "#0F7B7E", Money(-4500, "EUR")),
                    BalanceBarEntry("Bea", "#5B5FC7", Money(0, "EUR")),
                    BalanceBarEntry("Carlos", "#B26A00", Money(-8200, "EUR")),
                ),
            )
        }

        GallerySection("GroupCard") {
            GroupCard(
                title = "Viaje a Lisboa",
                subtitle = "4 personas · 12 gastos",
                avatarItems = listOf(
                    AvatarStackItem("Ana", "#E0533D"),
                    AvatarStackItem("Luis", "#0F7B7E"),
                    AvatarStackItem("Bea", "#5B5FC7"),
                    AvatarStackItem("Carlos", "#B26A00"),
                ),
                status = StatusChipStyle.UpToDate,
                statusLabel = "Al día",
                onMoreClick = {},
            )
            GroupCard(
                title = "Piso compartido",
                subtitle = "3 personas · 7 gastos",
                avatarItems = listOf(
                    AvatarStackItem("Ana", "#E0533D"),
                    AvatarStackItem("Luis", "#0F7B7E"),
                    AvatarStackItem("Bea", "#5B5FC7"),
                ),
                status = StatusChipStyle.Pending,
                statusLabel = "Pendiente",
                onMoreClick = {},
            )
        }

        GallerySection("ExpenseCard") {
            ExpenseCard(
                title = "Cena en el japonés",
                payerName = "Ana",
                payerColorHex = "#E0533D",
                metadata = "Pagó Ana · entre 4",
                amount = Money(8250, "EUR"),
                note = "Incluye propina",
                onMoreClick = {},
            )
        }

        GallerySection("ParticipantRow") {
            ParticipantRow(
                name = "Ana García",
                colorHex = "#E0533D",
                onEdit = {},
                onDelete = {},
            )
        }

        GallerySection("TransferCard") {
            TransferCard(
                fromName = "Luis",
                fromColorHex = "#0F7B7E",
                toName = "Ana",
                toColorHex = "#E0533D",
                amount = Money(1250, "EUR"),
            )
        }

        GallerySection("EmptyState") {
            EmptyState(
                title = "Aún no tienes grupos",
                body = "Crea el primero y empieza a repartir gastos.",
                icon = SplitItIcons.Group,
                ctaText = "Crear grupo",
                onCtaClick = {},
            )
        }

        GallerySection("SkeletonList") {
            SkeletonList(itemCount = 3)
        }

        GallerySection("ConfirmDeleteDialog") {
            var show by remember { mutableStateOf(true) }
            if (show) {
                ConfirmDeleteDialog(
                    title = "¿Eliminar grupo?",
                    message = "Esto elimina el grupo y sus datos locales.",
                    confirmLabel = stringResource(Res.string.delete),
                    onConfirm = { show = false },
                    onDismiss = { show = false },
                )
            }
        }
    }
}

@Composable
private fun GallerySection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}
