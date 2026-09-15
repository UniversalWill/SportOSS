package com.universalwill.sportoss.domain.model

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val mapLabelLanguage: MapLabelLanguage = MapLabelLanguage.APPLICATION,
)

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class AppLanguage(val languageTag: String?) {
    SYSTEM(null),
    RUSSIAN("ru"),
    ENGLISH("en"),
}

enum class MapLabelLanguage(val languageTag: String?) {
    APPLICATION(null),
    RUSSIAN("ru"),
    ENGLISH("en"),
}
