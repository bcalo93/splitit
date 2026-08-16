package com.splitit.localization

import platform.Foundation.NSBundle

actual class DeviceLocale actual constructor() {
    actual fun getLanguage(): String {
        val localizations = NSBundle.mainBundle.preferredLocalizations
        val preferred = if (localizations.isNotEmpty()) localizations.first() as? String else null
        return preferred?.take(2)?.lowercase() ?: "en"
    }
}
