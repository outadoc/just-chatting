package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.eygraber.uri.Uri
import com.kmpalette.DominantColorState
import com.kmpalette.loader.rememberNetworkLoader
import com.kmpalette.rememberDominantColorState
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.PaletteStyle
import fr.outadoc.justchatting.utils.http.toKtorUrl
import io.ktor.http.Url

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DynamicImageColorTheme(
    imageUrl: Uri?,
    content: @Composable () -> Unit,
) {
    val networkLoader = rememberNetworkLoader()
    val dominantColorState: DominantColorState<Url> =
        rememberDominantColorState(loader = networkLoader)

    LaunchedEffect(imageUrl) {
        val url = imageUrl?.toKtorUrl()
        if (url != null) {
            dominantColorState.updateFrom(url)
        }
    }

    DynamicMaterialExpressiveTheme(
        seedColor = dominantColorState.color,
        style = PaletteStyle.Expressive,
        motionScheme = MotionScheme.expressive(),
        animate = enableColorTransitions,
        content = content,
    )
}

/**
 * Whether to use transitions when loading a channel's Material theme colors.
 * Configurable in order to fix a desktop bug.
 */
internal expect val enableColorTransitions: Boolean
