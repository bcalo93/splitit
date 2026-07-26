package com.example.splitit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.splitit.data.database.AndroidDatabaseDriverFactory
import com.example.splitit.di.appModules
import org.koin.core.Koin
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        startSplitItKoinIfNeeded()

        setContent {
            App()
        }
    }

    private fun startSplitItKoinIfNeeded(): Koin {
        splitItKoin?.let { return it }

        return startKoin {
            modules(appModules(AndroidDatabaseDriverFactory(applicationContext)))
        }.koin.also {
            splitItKoin = it
        }
    }

    private companion object {
        var splitItKoin: Koin? = null
    }
}
