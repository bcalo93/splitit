package com.splitit.routes.groups.expenses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.model.Expense
import com.splitit.domain.model.Participant
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.GroupId
import com.splitit.presentation.expenses.ExpensesUiState
import com.splitit.presentation.expenses.ExpensesViewModel
import com.splitit.presentation.expenses.MILLIS_PER_DAY
import com.splitit.presentation.expenses.SplitMode
import com.splitit.presentation.expenses.civilDate
import com.splitit.presentation.expenses.startOfDay
import com.splitit.ui.components.AvatarBubble
import com.splitit.ui.components.ConfirmDeleteDialog
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.ExpenseCard
import com.splitit.ui.components.FormTextField
import com.splitit.ui.components.InlineErrorState
import com.splitit.ui.components.LoadingState
import com.splitit.ui.components.NoSearchResultsState
import com.splitit.ui.components.PrimaryButton
import com.splitit.ui.components.SearchField
import com.splitit.ui.components.ShareWeightStepper
import com.splitit.ui.components.SplitItIcons
import com.splitit.ui.components.SplitItScaffold
import com.splitit.ui.components.SplitItTopBar
import com.splitit.ui.theme.LocalSplitItMoneyStyles
import com.splitit.ui.theme.LocalSplitItSpacing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.add_expense
import splitit.composeapp.generated.resources.add_participants_before_expenses
import splitit.composeapp.generated.resources.day_full_date
import splitit.composeapp.generated.resources.day_today
import splitit.composeapp.generated.resources.day_yesterday
import splitit.composeapp.generated.resources.delete
import splitit.composeapp.generated.resources.delete_expense_message
import splitit.composeapp.generated.resources.delete_expense_title
import splitit.composeapp.generated.resources.edit
import splitit.composeapp.generated.resources.edit_expense
import splitit.composeapp.generated.resources.entity_expenses
import splitit.composeapp.generated.resources.expense_metadata
import splitit.composeapp.generated.resources.expenses
import splitit.composeapp.generated.resources.expenses_empty_title
import splitit.composeapp.generated.resources.month_short_1
import splitit.composeapp.generated.resources.month_short_2
import splitit.composeapp.generated.resources.month_short_3
import splitit.composeapp.generated.resources.month_short_4
import splitit.composeapp.generated.resources.month_short_5
import splitit.composeapp.generated.resources.month_short_6
import splitit.composeapp.generated.resources.month_short_7
import splitit.composeapp.generated.resources.month_short_8
import splitit.composeapp.generated.resources.month_short_9
import splitit.composeapp.generated.resources.month_short_10
import splitit.composeapp.generated.resources.month_short_11
import splitit.composeapp.generated.resources.month_short_12
import splitit.composeapp.generated.resources.new_expense
import splitit.composeapp.generated.resources.no_participants_yet
import splitit.composeapp.generated.resources.note
import splitit.composeapp.generated.resources.paid_by
import splitit.composeapp.generated.resources.payment_metadata
import splitit.composeapp.generated.resources.save
import splitit.composeapp.generated.resources.saving
import splitit.composeapp.generated.resources.search_expenses
import splitit.composeapp.generated.resources.select_all
import splitit.composeapp.generated.resources.split_between
import splitit.composeapp.generated.resources.split_mode
import splitit.composeapp.generated.resources.split_mode_equal
import splitit.composeapp.generated.resources.split_mode_weighted
import splitit.composeapp.generated.resources.title
import splitit.composeapp.generated.resources.total_parts
import splitit.composeapp.generated.resources.unknown

@Composable
fun ExpensesRoute(
    groupId: GroupId,
    onBack: () -> Unit,
    openExpenseForm: Boolean = false,
    viewModel: ExpensesViewModel = koinViewModel(
        parameters = { parametersOf(groupId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var formOpen by remember { mutableStateOf(openExpenseForm) }

    LaunchedEffect(groupId) {
        viewModel.refresh()
    }
    LaunchedEffect(state.saveSucceeded) {
        if (state.saveSucceeded) {
            viewModel.consumeSaveSuccess()
            formOpen = false
        }
    }

    if (formOpen) {
        ExpenseFormScreen(
            state = state,
            onBack = {
                viewModel.cancelEditing()
                formOpen = false
            },
            onTitleChange = viewModel::onTitleChange,
            onAmountChange = viewModel::onAmountChange,
            onNoteChange = viewModel::onNoteChange,
            onPayerSelected = viewModel::onPayerSelected,
            onParticipantToggled = viewModel::onParticipantToggled,
            onSelectAll = viewModel::selectAllParticipants,
            onSplitModeChanged = viewModel::onSplitModeChanged,
            onShareWeightChanged = viewModel::onShareWeightChanged,
            onSave = viewModel::save,
        )
    } else {
        ExpensesScreen(
            state = state,
            onBack = onBack,
            onAddExpense = { formOpen = true },
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onEdit = { expense ->
                viewModel.startEditing(expense)
                formOpen = true
            },
            onDelete = viewModel::delete,
            onRetry = viewModel::refresh,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpensesScreen(
    state: ExpensesUiState,
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEdit: (Expense) -> Unit,
    onDelete: (ExpenseId) -> Unit,
    onRetry: () -> Unit,
) {
    val spacing = LocalSplitItSpacing.current
    val participantById = remember(state.participants) {
        state.participants.associateBy { it.id }
    }
    val unknownLabel = stringResource(Res.string.unknown)

    SplitItScaffold(
        topBar = {
            SplitItTopBar(
                title = stringResource(Res.string.expenses),
                onBack = onBack,
            )
        },
        floatingActionButton = {
            if (state.participants.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddExpense,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = {
                        Icon(
                            painter = painterResource(SplitItIcons.Add),
                            contentDescription = null,
                        )
                    },
                    text = { Text(stringResource(Res.string.add_expense)) },
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading && state.expenses.isEmpty() -> LoadingState(
                    modifier = Modifier.align(Alignment.Center),
                )
                state.errorMessage != null && state.expenses.isEmpty() -> ErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (state.expenses.isNotEmpty() || state.searchQuery.isNotBlank()) {
                        SearchField(
                            query = state.searchQuery,
                            label = stringResource(Res.string.search_expenses),
                            onQueryChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.xl),
                        )
                    }
                    state.errorMessage?.let { message ->
                        InlineErrorState(message = message, onRetry = onRetry)
                    }
                    when {
                        state.participants.isEmpty() -> ExpensesWithoutParticipantsState(
                            modifier = Modifier.weight(1f),
                        )
                        state.expenses.isEmpty() -> ExpensesEmptyState(
                            onAddExpense = onAddExpense,
                            modifier = Modifier.weight(1f),
                        )
                        state.visibleExpenses.isEmpty() -> NoSearchResultsState(
                            query = state.searchQuery,
                            entityName = stringResource(Res.string.entity_expenses),
                            onClear = { onSearchQueryChange("") },
                            modifier = Modifier.weight(1f),
                        )
                        else -> LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = spacing.xl,
                                end = spacing.xl,
                                top = 4.dp,
                                bottom = 88.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(spacing.md),
                        ) {
                            state.groupedExpenses.forEach { group ->
                                stickyHeader(key = group.dayMillis) {
                                    DayHeader(
                                        label = dayLabel(group.dayMillis, state.nowMillis),
                                    )
                                }
                                items(
                                    items = group.expenses,
                                    key = { it.id.value },
                                    contentType = { "expense" },
                                ) { expense ->
                                    val payer = participantById[expense.payerId]
                                val recipient = if (expense.isTransferPayment) {
                                    expense.participantShares.firstOrNull()?.participantId
                                        ?.let { participantById[it] }
                                } else {
                                    null
                                }
                                ExpenseRowItem(
                                    expense = expense,
                                    payerName = payer?.name ?: unknownLabel,
                                    payerColorHex = payer?.avatarColor,
                                    recipientName = recipient?.name,
                                    onEdit = { onEdit(expense) },
                                    onDelete = { onDelete(expense.id) },
                                    modifier = Modifier.animateItem(),
                                )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(label: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
        )
    }
}

@Composable
private fun dayLabel(dayMillis: Long, nowMillis: Long): String {
    val todayStart = startOfDay(nowMillis)
    return when (dayMillis) {
        todayStart -> stringResource(Res.string.day_today)
        todayStart - MILLIS_PER_DAY -> stringResource(Res.string.day_yesterday)
        else -> {
            val (_, month, day) = civilDate(dayMillis / MILLIS_PER_DAY)
            stringResource(Res.string.day_full_date, monthShort(month), day)
        }
    }
}

@Composable
private fun monthShort(month: Int): String = when (month) {
    1 -> stringResource(Res.string.month_short_1)
    2 -> stringResource(Res.string.month_short_2)
    3 -> stringResource(Res.string.month_short_3)
    4 -> stringResource(Res.string.month_short_4)
    5 -> stringResource(Res.string.month_short_5)
    6 -> stringResource(Res.string.month_short_6)
    7 -> stringResource(Res.string.month_short_7)
    8 -> stringResource(Res.string.month_short_8)
    9 -> stringResource(Res.string.month_short_9)
    10 -> stringResource(Res.string.month_short_10)
    11 -> stringResource(Res.string.month_short_11)
    else -> stringResource(Res.string.month_short_12)
}

@Composable
private fun ExpenseRowItem(
    expense: Expense,
    payerName: String,
    payerColorHex: String?,
    recipientName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val metadata = if (expense.isTransferPayment && recipientName != null) {
        stringResource(Res.string.payment_metadata, recipientName)
    } else {
        stringResource(
            Res.string.expense_metadata,
            payerName,
            expense.participantShares.size,
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        ExpenseCard(
            title = expense.title,
            payerName = payerName,
            payerColorHex = payerColorHex,
            metadata = metadata,
            amount = expense.amount,
            note = expense.note,
            onMoreClick = { menuExpanded = true },
        )
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            if (!expense.isTransferPayment) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.edit)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(SplitItIcons.Edit),
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.delete)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(SplitItIcons.Delete),
                        contentDescription = null,
                    )
                },
                onClick = {
                    menuExpanded = false
                    showDeleteConfirmation = true
                },
            )
        }
    }

    if (showDeleteConfirmation) {
        ConfirmDeleteDialog(
            title = stringResource(Res.string.delete_expense_title),
            message = stringResource(Res.string.delete_expense_message),
            onConfirm = {
                showDeleteConfirmation = false
                onDelete()
            },
            onDismiss = { showDeleteConfirmation = false },
        )
    }
}

@Composable
private fun ExpenseFormScreen(
    state: ExpensesUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onPayerSelected: (ParticipantId) -> Unit,
    onParticipantToggled: (ParticipantId) -> Unit,
    onSelectAll: () -> Unit,
    onSplitModeChanged: (SplitMode) -> Unit,
    onShareWeightChanged: (ParticipantId, Int) -> Unit,
    onSave: () -> Unit,
) {
    val spacing = LocalSplitItSpacing.current
    val isEditing = state.editingExpenseId != null
    val amountFocusRequester = remember { FocusRequester() }

    SplitItScaffold(
        topBar = {
            SplitItTopBar(
                title = stringResource(
                    if (isEditing) Res.string.edit_expense else Res.string.new_expense,
                ),
                onBack = onBack,
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                PrimaryButton(
                    text = if (state.isSaving) {
                        stringResource(Res.string.saving)
                    } else {
                        stringResource(Res.string.save)
                    },
                    enabled = !state.isSaving && !state.isLoading,
                    isLoading = state.isSaving,
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = spacing.xl, vertical = 12.dp),
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.xl, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            AmountField(
                value = state.amount,
                currencyCode = state.editingCurrencyCode ?: state.defaultCurrencyCode,
                onValueChange = onAmountChange,
                error = state.amountError,
                focusRequester = amountFocusRequester,
            )

            FormTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = stringResource(Res.string.title),
                error = state.titleError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                label = { Text(stringResource(Res.string.note)) },
                maxLines = 3,
            )

            SectionLabel(stringResource(Res.string.paid_by))
            if (state.participants.isEmpty()) {
                Text(
                    text = stringResource(Res.string.add_participants_before_expenses),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                PayerSelector(
                    participants = state.participants,
                    selectedId = state.payerId,
                    onSelect = onPayerSelected,
                )
            }
            state.payerError?.let {
                ErrorLine(it)
            }

            SectionLabel(stringResource(Res.string.split_between))
            if (state.participants.isEmpty()) {
                Text(
                    text = stringResource(Res.string.add_participants_before_expenses),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                SplitChips(
                    participants = state.participants,
                    selectedIds = state.selectedParticipantIds,
                    onToggle = onParticipantToggled,
                    onSelectAll = onSelectAll,
                )
            }
            state.participantsError?.let {
                ErrorLine(it)
            }

            SectionLabel(stringResource(Res.string.split_mode))
            SplitModeSelector(
                mode = state.splitMode,
                onModeChange = onSplitModeChanged,
            )

            if (state.splitMode == SplitMode.Weighted) {
                Text(
                    text = stringResource(Res.string.total_parts, state.totalParts),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.participants
                    .filter { it.id in state.selectedParticipantIds }
                    .forEach { participant ->
                        WeightedParticipantCard(
                            participant = participant,
                            weight = state.shareWeights[participant.id] ?: 1,
                            resultAmount = state.weightedShareAmounts?.get(participant.id),
                            onWeightChange = { weight ->
                                onShareWeightChanged(participant.id, weight)
                            },
                        )
                    }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    LaunchedEffect(Unit) {
        amountFocusRequester.requestFocus()
    }
}

@Composable
private fun AmountField(
    value: String,
    currencyCode: String,
    onValueChange: (String) -> Unit,
    error: String?,
    focusRequester: FocusRequester,
) {
    val moneyStyles = LocalSplitItMoneyStyles.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        textStyle = moneyStyles.moneyHero.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        ),
        singleLine = true,
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        placeholder = {
            Text(
                text = "0",
                style = moneyStyles.moneyHero.copy(color = MaterialTheme.colorScheme.outline),
            )
        },
        suffix = {
            Text(
                text = currencyCode,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        supportingText = error?.let { message -> { Text(message) } },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ErrorLine(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun PayerSelector(
    participants: List<Participant>,
    selectedId: ParticipantId?,
    onSelect: (ParticipantId) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        participants.forEach { participant ->
            val selected = participant.id == selectedId
            AvatarBubble(
                name = participant.name,
                colorHex = participant.avatarColor,
                size = 48.dp,
                borderWidth = if (selected) 3.dp else 0.dp,
                borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified,
                modifier = Modifier.clickable { onSelect(participant.id) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SplitChips(
    participants: List<Participant>,
    selectedIds: Set<ParticipantId>,
    onToggle: (ParticipantId) -> Unit,
    onSelectAll: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SelectAllChip(onClick = onSelectAll)
        participants.forEach { participant ->
            ParticipantChip(
                participant = participant,
                selected = participant.id in selectedIds,
                onClick = { onToggle(participant.id) },
            )
        }
    }
}

@Composable
private fun SelectAllChip(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = stringResource(Res.string.select_all),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ParticipantChip(
    participant: Participant,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AvatarBubble(
                name = participant.name,
                colorHex = participant.avatarColor,
                size = 24.dp,
            )
            Text(
                text = participant.name,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SplitModeSelector(
    mode: SplitMode,
    onModeChange: (SplitMode) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = mode == SplitMode.Equal,
            onClick = { onModeChange(SplitMode.Equal) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) {
            Text(stringResource(Res.string.split_mode_equal))
        }
        SegmentedButton(
            selected = mode == SplitMode.Weighted,
            onClick = { onModeChange(SplitMode.Weighted) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = {
                Icon(
                    painter = painterResource(SplitItIcons.Tune),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        ) {
            Text(stringResource(Res.string.split_mode_weighted))
        }
    }
}

@Composable
private fun WeightedParticipantCard(
    participant: Participant,
    weight: Int,
    resultAmount: com.splitit.domain.value.Money?,
    onWeightChange: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AvatarBubble(
                    name = participant.name,
                    colorHex = participant.avatarColor,
                    size = 32.dp,
                )
                Text(
                    text = participant.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            ShareWeightStepper(
                value = weight,
                onValueChange = onWeightChange,
                resultAmount = resultAmount,
            )
        }
    }
}

@Composable
private fun ExpensesWithoutParticipantsState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.no_participants_yet),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.add_participants_before_expenses),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ExpensesEmptyState(
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ReceiptEmptyIllustration()
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.expenses_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = stringResource(Res.string.add_expense),
            onClick = onAddExpense,
        )
    }
}

@Composable
private fun ReceiptEmptyIllustration(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 88.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.shapes.medium,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(SplitItIcons.ReceiptLong),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(SplitItIcons.Check),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
