package com.splitit.localization

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
actual class StringResourceReader actual constructor() {
    actual fun readStringsXml(locale: String): String? {
        val fileName = "strings.xml"
        val subdirectory = if (locale == "en") "values" else "values-$locale"
        val path = NSBundle.mainBundle.pathForResource(
            name = fileName,
            ofType = null,
            inDirectory = "composeResources/$subdirectory"
        ) ?: return null
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as? String
    }
}
