package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.runtime.Composable
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest

@Composable
internal actual fun remoteImageModel(url: String?): ImageRequest {
    val context = LocalPlatformContext.current
    return ImageRequest.Builder(context)
        .data(url)
        .build()
}
