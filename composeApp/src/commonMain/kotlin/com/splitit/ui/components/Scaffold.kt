package com.splitit.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import splitit.composeapp.generated.resources.Res
import splitit.composeapp.generated.resources.cd_back

private const val BACK_DEBOUNCE_MS = 350L

@Composable
fun SplitItScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitItTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    navigationIcon: DrawableResource = SplitItIcons.ArrowBack,
    navigationContentDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    var backBlocked by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = {
                        if (!backBlocked) {
                            backBlocked = true
                            onBack()
                        }
                    },
                    enabled = !backBlocked,
                ) {
                    Icon(
                        painter = painterResource(navigationIcon),
                        contentDescription = navigationContentDescription
                            ?: stringResource(Res.string.cd_back),
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )

    if (backBlocked) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            delay(BACK_DEBOUNCE_MS)
            backBlocked = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitItLargeTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    var backBlocked by remember { mutableStateOf(false) }

    LargeTopAppBar(
        modifier = modifier,
        title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = {
                        if (!backBlocked) {
                            backBlocked = true
                            onBack()
                        }
                    },
                    enabled = !backBlocked,
                ) {
                    Icon(
                        painter = painterResource(SplitItIcons.ArrowBack),
                        contentDescription = stringResource(Res.string.cd_back),
                    )
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )

    if (backBlocked) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            delay(BACK_DEBOUNCE_MS)
            backBlocked = false
        }
    }
}
