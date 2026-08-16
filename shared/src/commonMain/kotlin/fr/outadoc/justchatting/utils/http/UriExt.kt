package fr.outadoc.justchatting.utils.http

import com.eygraber.uri.Uri

public fun String.toUri(): Uri = Uri.parse(this)
