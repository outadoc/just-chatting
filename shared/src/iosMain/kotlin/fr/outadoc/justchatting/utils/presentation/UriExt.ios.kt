package fr.outadoc.justchatting.utils.presentation

import com.eygraber.uri.Uri
import platform.Foundation.NSURL

/**
 * [Uri] isn't exported to Swift, which only sees it as an opaque object: convert it before
 * exposing it.
 */
internal fun Uri.toNSURL(): NSURL? = NSURL.URLWithString(toString())
