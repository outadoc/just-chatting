package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.transformations
import fr.outadoc.justchatting.feature.chat.presentation.CoilReducedAnimationTransformation
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
internal actual fun remoteImageModel(url: String?): ImageRequest {
    val context = LocalPlatformContext.current

    return ImageRequest
        .Builder(context)
        .apply {
            if (UIAccessibilityIsReduceMotionEnabled()) {
                transformations(CoilReducedAnimationTransformation())
            }
        }.data(url)
        .build()
}
