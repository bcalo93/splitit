package com.splitit.localization

actual class StringResourceReader actual constructor() {
    actual fun readStringsXml(locale: String): String? {
        val path = if (locale == "en") {
            "composeResources/values/strings.xml"
        } else {
            "composeResources/values-$locale/strings.xml"
        }
        return this::class.java.classLoader?.getResourceAsStream(path)?.bufferedReader()?.readText()
    }
}
