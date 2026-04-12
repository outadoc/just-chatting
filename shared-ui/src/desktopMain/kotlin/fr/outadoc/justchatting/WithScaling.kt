package fr.outadoc.justchatting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Work around Linux scaling issue.
 *
 * [CMP-6004](https://youtrack.jetbrains.com/issue/CMP-6004)
 */
@Composable
internal fun WithScaling(content: @Composable () -> Unit) {
    val override = System.getProperty("sun.java2d.uiScale.enabled", "false") == "true"
    val density = System.getProperty("sun.java2d.uiScale", "1.0").toFloat()

    if (override) {
        CompositionLocalProvider(LocalDensity provides Density(density)) {
            content()
        }
    } else {
        content()
    }
}
