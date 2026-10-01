package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.transformations
import fr.outadoc.justchatting.feature.chat.presentation.CoilReducedAnimationTransformation

/**
 * Whether animated images should actually be animated, as chosen by the user in the app's
 * appearance settings.
 */
@Immutable
internal data class ImageAnimationPreferences(
    val animateEmotes: Boolean = true,
    val animateGifs: Boolean = true,
)

internal val LocalImageAnimationPreferences =
    staticCompositionLocalOf { ImageAnimationPreferences() }

/**
 * @param animate if false, only the first frame of an animated image is displayed.
 */
@Composable
internal fun remoteImageModel(
    url: String?,
    animate: Boolean = true,
): ImageRequest {
    val context = LocalPlatformContext.current

    return ImageRequest
        .Builder(context)
        .apply {
            if (!animate) {
                transformations(CoilReducedAnimationTransformation())
            }
        }.data(url)
        .build()
}
