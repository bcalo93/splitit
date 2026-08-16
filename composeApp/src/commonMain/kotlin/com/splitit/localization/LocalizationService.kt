package com.splitit.localization

interface LocalizationService {
    fun getString(key: LocalizedString): String
}

expect class StringResourceReader() {
    fun readStringsXml(locale: String): String?
}

class DefaultLocalizationService(
    private val deviceLocale: DeviceLocale,
    private val resourceReader: StringResourceReader,
) : LocalizationService {
    private val cache = mutableMapOf<String, Map<String, String>>()

    override fun getString(key: LocalizedString): String {
        val locale = deviceLocale.getLanguage()
        val strings = cache.getOrPut(locale) { loadStrings(locale) }
        return strings[key.key] ?: key.key
    }

    private fun loadStrings(locale: String): Map<String, String> {
        val xml = resourceReader.readStringsXml(locale)
            ?: resourceReader.readStringsXml("en")
            ?: return emptyMap()
        return parseStringsXml(xml)
    }

    private fun parseStringsXml(xml: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val regex = Regex("""<string name="([^"]+)">([^<]*)</string>""")
        regex.findAll(xml).forEach { match ->
            val name = match.groupValues[1]
            val value = match.groupValues[2]
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
            result[name] = value
        }
        return result
    }
}
