package fr.outadoc.justchatting

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import javax.swing.UIManager
import java.awt.Color as AwtColor

internal val isMacOs: Boolean =
    System.getProperty("os.name")?.contains("Mac", ignoreCase = true) == true

/**
 * The native menu bar is rendered by Swing, which doesn't pick up our Compose Material theme
 * on its own — without this, it stays on its default light look even in dark mode.
 */
internal fun applyMenuBarTheme(colorScheme: ColorScheme) {
    val background = AwtColor(colorScheme.surface.toArgb(), true)
    val foreground = AwtColor(colorScheme.onSurface.toArgb(), true)
    val selectionBackground = AwtColor(colorScheme.surfaceVariant.toArgb(), true)
    val selectionForeground = AwtColor(colorScheme.onSurfaceVariant.toArgb(), true)

    val themedComponents =
        listOf("MenuBar", "Menu", "MenuItem", "PopupMenu", "CheckBoxMenuItem", "RadioButtonMenuItem")

    for (component in themedComponents) {
        UIManager.put("$component.background", background)
        UIManager.put("$component.foreground", foreground)
        UIManager.put("$component.selectionBackground", selectionBackground)
        UIManager.put("$component.selectionForeground", selectionForeground)
        UIManager.put("$component.borderColor", background)
    }
}
