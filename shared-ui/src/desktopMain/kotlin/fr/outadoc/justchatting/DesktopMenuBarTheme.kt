package fr.outadoc.justchatting

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf

internal val isMacOs: Boolean =
    System
        .getProperty("os.name")
        ?.contains("Mac", ignoreCase = true) == true

internal fun applySwingTheme(isDarkTheme: Boolean) {
    if (isDarkTheme) {
        FlatDarkLaf.setup()
    } else {
        FlatLightLaf.setup()
    }
}
