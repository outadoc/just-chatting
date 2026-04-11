package fr.outadoc.justchatting.utils.http

import com.eygraber.uri.Uri
import io.ktor.http.Url

public fun Uri.toKtorUrl(): Url = Url(toString())

public fun String.toUri(): Uri = Uri.parse(this)
