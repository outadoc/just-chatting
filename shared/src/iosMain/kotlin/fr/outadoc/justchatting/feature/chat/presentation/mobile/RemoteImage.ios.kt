package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.runtime.Composable
import coil3.PlatformContext
import coil3.request.ImageRequest

@Composable
internal actual fun remoteImageModel(url: String?): ImageRequest {
    return ImageRequest.Builder(PlatformContext.INSTANCE)
        .data(url)
        .build()
}
