package fr.outadoc.justchatting.utils.presentation

import android.os.Build

internal actual fun areBubblesSupported(): Boolean {
    return Build.VERSION.SDK_INT >= 29
}
