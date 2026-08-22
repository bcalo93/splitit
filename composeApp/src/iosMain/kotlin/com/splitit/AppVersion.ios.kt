package com.splitit

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle

@Composable
actual fun appVersion(): String {
    return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        ?: "1.0"
}
