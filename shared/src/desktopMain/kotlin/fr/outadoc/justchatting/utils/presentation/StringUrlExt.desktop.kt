package fr.outadoc.justchatting.utils.presentation

import java.net.MalformedURLException
import java.net.URL

public actual fun String.isValidWebUrl(): Boolean =
    try {
        URL(this)
        true
    } catch (e: MalformedURLException) {
        false
    }
