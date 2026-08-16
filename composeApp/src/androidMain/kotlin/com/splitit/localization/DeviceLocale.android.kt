package com.splitit.localization

import java.util.Locale

actual class DeviceLocale actual constructor() {
    actual fun getLanguage(): String {
        return Locale.getDefault().language.take(2).lowercase()
    }
}
