package fr.outadoc.justchatting.util

import androidx.palette.graphics.Palette
import kotlin.coroutines.suspendCoroutine

suspend fun Palette.Builder.generateAsync(): Palette? {
    return suspendCoroutine { cont ->
        generate { palette -> cont.resumeWith(Result.success(palette)) }
    }
}
