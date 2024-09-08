package fr.outadoc.justchatting.utils.presentation

import android.util.Patterns

internal actual object Patterns {
    actual val WebUrlRegex: Regex
        get() = Patterns.WEB_URL.toRegex()
}
