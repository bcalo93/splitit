package com.example.splitit

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.splitit.di.modules
import com.example.splitit.presentation.ParticipantList
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication


@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(modules)
    }) {
        MaterialTheme {
            ParticipantList()
        }
    }
}