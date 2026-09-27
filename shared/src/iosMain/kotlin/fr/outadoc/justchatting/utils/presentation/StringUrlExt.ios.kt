@file:OptIn(ExperimentalForeignApi::class)

package fr.outadoc.justchatting.utils.presentation

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSDataDetector
import platform.Foundation.NSMakeRange
import platform.Foundation.NSTextCheckingTypeLink
import platform.Foundation.URL
import platform.Foundation.firstMatchInString

private val linkDetector: NSDataDetector? by lazy {
    NSDataDetector(types = NSTextCheckingTypeLink, error = null)
}

/**
 * Whether this whole string is a web link, with or without its scheme (like `example.com`), as
 * `Patterns.WEB_URL` does on Android.
 */
public actual fun String.isValidWebUrl(): Boolean {
    val wordLength = length.toULong()
    val match =
        linkDetector?.firstMatchInString(
            string = this,
            options = 0u,
            range = NSMakeRange(0u, wordLength),
        ) ?: return false

    val coversWholeString = match.range.useContents { location == 0uL && length == wordLength }
    val scheme = match.URL?.scheme?.lowercase()
    return coversWholeString && (scheme == "http" || scheme == "https")
}
