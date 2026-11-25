package com.iustinmoraru.prtracker.data.settings

/** Theme chosen by the user in the settings screen. [SYSTEM] follows the device theme. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        /** Parses a stored value, falling back to [SYSTEM] for unknown or missing values. */
        fun fromStorage(value: String?): ThemeMode = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}
