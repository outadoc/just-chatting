package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.eygraber.uri.Uri
import com.kmpalette.DominantColorState
import com.kmpalette.loader.rememberNetworkLoader
import com.kmpalette.rememberDominantColorState
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import fr.outadoc.justchatting.utils.http.toKtorUrl
import io.ktor.http.Url

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

    DynamicMaterialTheme(
        seedColor = dominantColorState.color,
        style = PaletteStyle.Vibrant,
        animate = true,
        content = content,
    )
}
