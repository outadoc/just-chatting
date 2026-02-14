package fr.outadoc.justchatting.utils.presentation

import android.util.Patterns

private val UrlRegex = Patterns.WEB_URL.toRegex()

internal actual fun String.isValidWebUrl(): Boolean = matches(UrlRegex)
