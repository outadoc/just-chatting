package fr.outadoc.justchatting.utils.presentation

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun String.isValidWebUrl(): Boolean {
    val url = NSURL.URLWithString(this) ?: return false
    return UIApplication.sharedApplication.canOpenURL(url)
}
