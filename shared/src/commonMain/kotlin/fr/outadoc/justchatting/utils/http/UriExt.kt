package fr.outadoc.justchatting.utils.http

import com.eygraber.uri.Uri
import io.ktor.http.Url

internal fun Uri.toKtorUrl(): Url {
    return Url(toString())
}

internal fun String.toUri(): Uri {
    return Uri.parse(this)
}
