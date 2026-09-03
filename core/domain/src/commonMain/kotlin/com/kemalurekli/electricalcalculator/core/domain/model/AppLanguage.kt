package com.kemalurekli.electricalcalculator.core.domain.model

/**
 * A language the app ships translations for.
 *
 * [languageTag] is a BCP-47 tag, or `null` for [SYSTEM], which means "follow the
 * device". Adding a language is four edits: a constant here, a matching
 * `values-<tag>` folder, an entry in `res/xml/locales_config.xml`, and one in
 * `iosApp/Info.plist` under `CFBundleLocalizations` — iOS only offers the
 * per-app language row for languages the bundle declares.
 *
 * And one check that is not an edit: the bundled typefaces are subset to the
 * characters the app displays, so a language in a script they do not carry
 * renders in the platform's own font instead. `scripts/build_fonts.py` names
 * the ranges; `FontCoverageTest` fails if a shipped language outruns them.
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

    // Ordered by the endonym the picker shows, not by speaker count or by when
    // they were added: a reader scanning for their own language is scanning an
    // alphabet, and "Deutsch" belongs where D is.
    GERMAN("de"),

    SPANISH("es"),

    FRENCH("fr"),

    INDONESIAN("id"),

    ITALIAN("it"),

    DUTCH("nl"),

    POLISH("pl"),

    PORTUGUESE("pt"),

    VIETNAMESE("vi"),

    RUSSIAN("ru"),
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
