package com.example.splitit

import androidx.compose.ui.window.ComposeUIViewController
import com.example.splitit.data.database.NativeDatabaseDriverFactory
import com.example.splitit.di.appModules
import org.koin.core.Koin
import org.koin.core.context.startKoin

private var splitItKoin: Koin? = null

fun MainViewController() = ComposeUIViewController {
    startSplitItKoinIfNeeded()
    App()
}

private fun startSplitItKoinIfNeeded(): Koin {
    splitItKoin?.let { return it }

    return startKoin {
        modules(appModules(NativeDatabaseDriverFactory()))
    }.koin.also {
        splitItKoin = it
    }
}
