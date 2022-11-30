package fr.outadoc.justchatting.utils.ui

import androidx.palette.graphics.Palette
import kotlin.coroutines.suspendCoroutine

suspend fun Palette.Builder.generateAsync(): Palette? {
    return suspendCoroutine { cont ->
        generate { palette -> cont.resumeWith(Result.success(palette)) }
    }
}
