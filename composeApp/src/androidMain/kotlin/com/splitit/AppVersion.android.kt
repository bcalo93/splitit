package com.splitit

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun appVersion(): String {
    val context = LocalContext.current
    return try {
        val versionName = context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
        versionName ?: "1.0"
    } catch (e: Exception) {
        "1.0"
    }
}
