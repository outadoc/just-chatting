package fr.outadoc.justchatting.utils.presentation

import java.net.MalformedURLException
import java.net.URL

internal actual fun String.isValidWebUrl(): Boolean {
    return try {
        URL(this)
        true
    } catch (e: MalformedURLException) {
        false
    }
}
