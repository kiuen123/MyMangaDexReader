package com.example.mymangadexreader.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LanguageOption(val code: String, val displayName: String, val flag: String)

object LanguagePreference {

    val supportedLanguages = listOf(
        LanguageOption("all", "Tất cả ngôn ngữ", "🌐"),
        LanguageOption("vi", "Tiếng Việt", "🇻🇳"),
        LanguageOption("en", "English", "🇬🇧"),
        LanguageOption("ja", "日本語", "🇯🇵"),
        LanguageOption("zh", "中文 (简体)", "🇨🇳"),
        LanguageOption("zh-hk", "中文 (繁體)", "🇹🇼"),
        LanguageOption("ko", "한국어", "🇰🇷"),
        LanguageOption("fr", "Français", "🇫🇷"),
        LanguageOption("es", "Español", "🇪🇸"),
        LanguageOption("es-la", "Español (LA)", "🌎"),
        LanguageOption("pt-br", "Português (BR)", "🇧🇷"),
        LanguageOption("ru", "Русский", "🇷🇺"),
        LanguageOption("de", "Deutsch", "🇩🇪"),
        LanguageOption("id", "Bahasa Indonesia", "🇮🇩"),
        LanguageOption("th", "ภาษาไทย", "🇹🇭"),
        LanguageOption("ar", "العربية", "🇸🇦"),
        LanguageOption("it", "Italiano", "🇮🇹"),
        LanguageOption("tr", "Türkçe", "🇹🇷"),
        LanguageOption("pl", "Polski", "🇵🇱"),
    )

    private val _selectedLanguage = MutableStateFlow(supportedLanguages[1]) // default: Tiếng Việt
    val selectedLanguageFlow: StateFlow<LanguageOption> = _selectedLanguage.asStateFlow()

    val selectedLanguage: LanguageOption get() = _selectedLanguage.value
    val code: String get() = _selectedLanguage.value.code

    fun setLanguage(lang: LanguageOption) {
        _selectedLanguage.value = lang
    }

    fun findByCode(code: String): LanguageOption =
        supportedLanguages.firstOrNull { it.code == code } ?: supportedLanguages.first()
}

