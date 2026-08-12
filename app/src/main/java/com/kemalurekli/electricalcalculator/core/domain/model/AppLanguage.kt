package com.kemalurekli.electricalcalculator.core.domain.model

/**
 * A language the app ships translations for.
 *
 * [languageTag] is a BCP-47 tag, or `null` for [SYSTEM], which means "follow the
 * device". Adding a language is three edits: a constant here, a matching
 * `values-<tag>` folder, and an entry in `res/xml/locales_config.xml`.
 *
 * The enum deliberately carries no display name. A language picker must label
 * each option in *that* language ("Türkçe", not "Turkish") — otherwise a user
 * who cannot read the current language cannot find their own. The UI derives
 * those names from the tag via `java.util.Locale`.
 */
enum class AppLanguage(val languageTag: String?) {
    /** Follow the device language, falling back to English if unsupported. */
    SYSTEM(null),

    ENGLISH("en"),

    TURKISH("tr"),
    ;

    companion object {
        /**
         * Resolves a stored tag back to a constant.
         *
         * Matches on the primary language subtag, so a stored `tr-TR` still
         * resolves to [TURKISH]. An unknown tag falls back to [SYSTEM] rather
         * than throwing — it can occur if a language is withdrawn in a later
         * release while a device still holds the old preference.
         */
        fun fromTagOrSystem(tag: String?): AppLanguage {
            if (tag.isNullOrBlank()) return SYSTEM
            val primary = tag.substringBefore('-').lowercase()
            return entries.firstOrNull { it.languageTag == primary } ?: SYSTEM
        }
    }
}
