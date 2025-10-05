package com.example.splitit.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.splitit.storage.Storage
import org.koin.compose.koinInject

@Composable
fun ParticipantList(storage: Storage = koinInject()) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Hello Users")
        Text("Participant Count: ${storage.participants.size}")
        storage.participants.forEach{
            Text(it.nickname)
        }
    }

}