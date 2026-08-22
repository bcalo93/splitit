package com.splitit.routes.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.value.GroupId
import com.splitit.presentation.groups.GroupListUiState
import com.splitit.presentation.groups.GroupListViewModel
import com.splitit.ui.components.AvatarBubble
import com.splitit.ui.components.AvatarStackItem
import com.splitit.ui.components.ConfirmDeleteDialog
import com.splitit.ui.components.ErrorState
import com.splitit.ui.components.GroupCard
import com.splitit.ui.components.InlineErrorState
import com.splitit.ui.components.LoadingState
import com.splitit.ui.components.NoSearchResultsState
import com.splitit.ui.components.PrimaryButton
import com.splitit.ui.components.SearchField
import com.splitit.ui.components.SplitItIcons
import com.splitit.ui.components.SplitItLargeTopBar
import com.splitit.ui.components.SplitItScaffold
import com.splitit.ui.components.StatusChipStyle
import com.splitit.ui.theme.LocalSplitItSpacing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.cd_settings
import splitit.composeapp.generated.resources.create_first_group
import splitit.composeapp.generated.resources.delete
import splitit.composeapp.generated.resources.delete_group_message
import splitit.composeapp.generated.resources.delete_group_title
import splitit.composeapp.generated.resources.edit
import splitit.composeapp.generated.resources.entity_groups
import splitit.composeapp.generated.resources.group_status_pending
import splitit.composeapp.generated.resources.group_status_up_to_date
import splitit.composeapp.generated.resources.group_summary
import splitit.composeapp.generated.resources.groups_title
import splitit.composeapp.generated.resources.new_group
import splitit.composeapp.generated.resources.no_groups_yet
import splitit.composeapp.generated.resources.search_groups

@Composable
fun GroupsRoute(
    onCreate: () -> Unit,
    onOpen: (GroupId) -> Unit,
    onEdit: (GroupId) -> Unit,
    onSettings: () -> Unit,
    viewModel: GroupListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    GroupsScreen(
        state = state,
        onCreate = onCreate,
        onOpen = onOpen,
        onEdit = onEdit,
        onSettings = onSettings,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onDelete = viewModel::delete,
        onRetry = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupsScreen(
    state: GroupListUiState,
    onCreate: () -> Unit,
    onOpen: (GroupId) -> Unit,
    onEdit: (GroupId) -> Unit,
    onSettings: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDelete: (GroupId) -> Unit,
    onRetry: () -> Unit,
) {
    val spacing = LocalSplitItSpacing.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val fabExpanded = scrollBehavior.state.collapsedFraction < 0.5f

    SplitItScaffold(
        topBar = {
            SplitItLargeTopBar(
                title = stringResource(Res.string.groups_title),
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            painter = painterResource(SplitItIcons.Settings),
                            contentDescription = stringResource(Res.string.cd_settings),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                expanded = fabExpanded,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        painter = painterResource(SplitItIcons.Add),
                        contentDescription = null,
                    )
                },
                text = { Text(stringResource(Res.string.new_group)) },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading && state.groups.isEmpty() -> LoadingState(
                    modifier = Modifier.align(Alignment.Center),
                )
                state.errorMessage != null && state.groups.isEmpty() -> ErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (state.groups.isNotEmpty() || state.searchQuery.isNotBlank()) {
                        SearchField(
                            query = state.searchQuery,
                            label = stringResource(Res.string.search_groups),
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
                        state.groups.isEmpty() -> GroupsEmptyState(
                            onCreate = onCreate,
                            modifier = Modifier.weight(1f),
                        )
                        state.visibleGroups.isEmpty() -> NoSearchResultsState(
                            query = state.searchQuery,
                            entityName = stringResource(Res.string.entity_groups),
                            onClear = { onSearchQueryChange("") },
                            modifier = Modifier.weight(1f),
                        )
                        else -> LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .nestedScroll(scrollBehavior.nestedScrollConnection),
                            contentPadding = PaddingValues(
                                start = spacing.xl,
                                end = spacing.xl,
                                top = 12.dp,
                                bottom = 88.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = state.visibleGroups,
                                key = { it.id.value },
                                contentType = { "group" },
                            ) { group ->
                                GroupRow(
                                    group = group,
                                    participants = state.participantsByGroup[group.id].orEmpty(),
                                    isPending = group.id in state.pendingGroupIds,
                                    onOpen = { onOpen(group.id) },
                                    onEdit = { onEdit(group.id) },
                                    onDelete = { onDelete(group.id) },
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

@Composable
private fun GroupRow(
    group: ExpenseGroup,
    participants: List<Participant>,
    isPending: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val avatarItems = participants.map { AvatarStackItem(name = it.name, colorHex = it.avatarColor) }

    Box(modifier = modifier.fillMaxWidth()) {
        GroupCard(
            title = group.title,
            subtitle = stringResource(
                Res.string.group_summary,
                group.participantIds.size,
                group.expenseIds.size,
            ),
            avatarItems = avatarItems,
            status = if (isPending) StatusChipStyle.Pending else StatusChipStyle.UpToDate,
            statusLabel = stringResource(
                if (isPending) Res.string.group_status_pending else Res.string.group_status_up_to_date,
            ),
            onClick = onOpen,
            onMoreClick = { menuExpanded = true },
        )
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
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
            title = stringResource(Res.string.delete_group_title),
            message = stringResource(Res.string.delete_group_message),
            onConfirm = {
                showDeleteConfirmation = false
                onDelete()
            },
            onDismiss = { showDeleteConfirmation = false },
        )
    }
}

@Composable
private fun GroupsEmptyState(
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        GroupsEmptyIllustration()
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.no_groups_yet),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = stringResource(Res.string.create_first_group),
            onClick = onCreate,
        )
    }
}

@Composable
private fun GroupsEmptyIllustration(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(width = 152.dp, height = 104.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 48.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp,
                    ),
                )
                .background(MaterialTheme.colorScheme.primaryContainer),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(SplitItIcons.Check),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Row(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy((-12).dp),
        ) {
            AvatarBubble(name = "Ana", colorHex = "#E0533D", size = 40.dp)
            AvatarBubble(name = "Bea", colorHex = "#0F7B7E", size = 40.dp)
            AvatarBubble(name = "Cris", colorHex = "#5B5FC7", size = 40.dp)
        }
    }
}
