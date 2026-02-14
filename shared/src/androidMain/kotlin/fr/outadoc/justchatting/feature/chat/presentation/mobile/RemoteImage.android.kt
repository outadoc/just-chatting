package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.request.ImageRequest
import coil3.request.transformations
import fr.outadoc.justchatting.feature.chat.presentation.CoilReducedAnimationTransformation

@Composable
internal actual fun remoteImageModel(url: String?): ImageRequest {
    val context = LocalContext.current

    val systemAnimationScale: Float =
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f,
        )

    return ImageRequest
        .Builder(context)
        .apply {
            if (systemAnimationScale == 0f) {
                transformations(CoilReducedAnimationTransformation())
            }
        }.data(url)
        .build()
}
