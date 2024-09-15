package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import coil3.BitmapImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.google.android.material.color.utilities.QuantizerCelebi
import com.google.android.material.color.utilities.Scheme
import com.google.android.material.color.utilities.Score
import fr.outadoc.justchatting.utils.presentation.isDark
import kotlinx.coroutines.suspendCancellableCoroutine

@Stable
@Composable
internal actual fun dynamicImageColorScheme(
    url: String?,
    parentScheme: ColorScheme,
): ColorScheme {
    val context = LocalContext.current
    var sourceColor: Color? by remember { mutableStateOf(null) }

    LaunchedEffect(url) {
        val logo: Bitmap? = url?.let {
            loadImageToBitmap(
                context = context,
                imageUrl = url,
                width = 32,
                height = 32,
            )
        }

        sourceColor = logo?.getSourceColor()
    }

    return sourceColor?.let { currentSourceColor ->
        if (parentScheme.isDark) {
            darkSchemeFromColor(currentSourceColor)
        } else {
            lightSchemeFromColor(currentSourceColor)
        }
    } ?: parentScheme
}

@Composable
@Stable
internal actual fun singleSourceColorScheme(
    color: Color?,
    parentScheme: ColorScheme,
): ColorScheme {
    return color?.let { currentSourceColor ->
        if (parentScheme.isDark) {
            darkSchemeFromColor(currentSourceColor)
        } else {
            lightSchemeFromColor(currentSourceColor)
        }
    } ?: parentScheme
}

@SuppressLint("RestrictedApi")
private fun Bitmap.getSourceColor(): Color {
    val width: Int = width
    val height: Int = height

    val intArray = IntArray(width * height)

    getPixels(
        /* pixels = */
        intArray,
        /* offset = */
        0,
        /* stride = */
        width,
        /* x = */
        0,
        /* y = */
        0,
        /* width = */
        width,
        /* height = */
        height,
    )

    val quantized = QuantizerCelebi.quantize(intArray, 128)
    val score = Score.score(quantized)
    val sourceColor = score.first()

    return Color(sourceColor)
}

@SuppressLint("RestrictedApi")
private fun darkSchemeFromColor(color: Color): ColorScheme {
    return Scheme.dark(color.toArgb()).toComposeTheme()
}

@SuppressLint("RestrictedApi")
private fun lightSchemeFromColor(color: Color): ColorScheme {
    return Scheme.light(color.toArgb()).toComposeTheme()
}

@SuppressLint("RestrictedApi")
internal fun Scheme.toComposeTheme(): ColorScheme {
    return ColorScheme(
        primary = Color(primary),
        onPrimary = Color(onPrimary),
        primaryContainer = Color(primaryContainer),
        onPrimaryContainer = Color(onPrimaryContainer),
        inversePrimary = Color(inversePrimary),
        secondary = Color(secondary),
        onSecondary = Color(onSecondary),
        secondaryContainer = Color(secondaryContainer),
        onSecondaryContainer = Color(onSecondaryContainer),
        tertiary = Color(tertiary),
        onTertiary = Color(onTertiary),
        tertiaryContainer = Color(tertiaryContainer),
        onTertiaryContainer = Color(onTertiaryContainer),
        background = Color(background),
        onBackground = Color(onBackground),
        surface = Color(surface),
        onSurface = Color(onSurface),
        surfaceVariant = Color(surfaceVariant),
        onSurfaceVariant = Color(onSurfaceVariant),
        surfaceTint = Color(primary),
        inverseSurface = Color(inverseSurface),
        inverseOnSurface = Color(inverseOnSurface),
        error = Color(error),
        onError = Color(onError),
        errorContainer = Color(errorContainer),
        onErrorContainer = Color(onErrorContainer),
        outline = Color(outline),
        outlineVariant = Color(outlineVariant),
        scrim = Color(scrim),
        surfaceBright = Color(surface),
        surfaceDim = Color(surface),
        surfaceContainer = Color(surface),
        surfaceContainerHigh = Color(surface),
        surfaceContainerHighest = Color(surfaceVariant),
        surfaceContainerLow = Color(surface),
        surfaceContainerLowest = Color(surface),
    )
}

private suspend fun loadImageToBitmap(
    context: Context,
    imageUrl: String,
    width: Int,
    height: Int,
): Bitmap? {
    return suspendCancellableCoroutine { cont ->
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(width, height)
            .allowHardware(false)
            .target(
                onSuccess = { drawable ->
                    cont.resumeWith(
                        Result.success((drawable as? BitmapImage)?.bitmap),
                    )
                },
                onError = { drawable ->
                    cont.resumeWith(
                        Result.success((drawable as? BitmapImage)?.bitmap),
                    )
                },
            )
            .build()

        if (!cont.isCancelled) {
            val disposable = context.imageLoader.enqueue(request)
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }
}
