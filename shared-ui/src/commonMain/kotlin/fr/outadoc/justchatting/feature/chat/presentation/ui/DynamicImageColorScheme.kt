package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.eygraber.uri.Uri
import com.kmpalette.rememberPainterDominantColorState
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import fr.outadoc.justchatting.utils.presentation.isAppInDarkTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DynamicImageColorTheme(
    imageUrl: Uri?,
    content: @Composable () -> Unit,
) {
    val dominantColorState = rememberPainterDominantColorState()
    val scope = rememberCoroutineScope()
    val context = LocalPlatformContext.current

    val request =
        remember(context, imageUrl) {
            ImageRequest
                .Builder(context)
                .data(imageUrl?.toString())
                .allowSoftwareRendering()
                .build()
        }

    // Loaded through Coil so that any scheme it supports (http, local resources, etc.)
    // can feed the palette, instead of being limited to plain network URLs.
    rememberAsyncImagePainter(
        model = request,
        onSuccess = { state ->
            scope.launch {
                dominantColorState.updateFrom(state.painter)
            }
        },
    )

    DynamicMaterialTheme(
        seedColor = dominantColorState.color,
        style = PaletteStyle.Expressive,
        animate = enableColorTransitions,
        content = content,
        isDark = isAppInDarkTheme(),
    )
}

/**
 * Whether to use transitions when loading a channel's Material theme colors.
 * Configurable in order to fix a desktop bug.
 */
internal expect val enableColorTransitions: Boolean

/**
 * Makes sure the loaded image can be drawn to an off-screen, software-backed canvas, which is
 * what the palette extraction does. Hardware bitmaps can't be drawn there, which would make
 * palette generation fail silently and fall back to the default theme colors.
 */
internal expect fun ImageRequest.Builder.allowSoftwareRendering(): ImageRequest.Builder
