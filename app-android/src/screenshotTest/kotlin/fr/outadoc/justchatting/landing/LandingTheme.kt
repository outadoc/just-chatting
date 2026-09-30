package fr.outadoc.justchatting.landing

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.utils.presentation.AppTheme

/** A phone, in light and dark themes. */
@Preview(name = "light", device = "spec:width=411dp,height=891dp")
@Preview(name = "dark", device = "spec:width=411dp,height=891dp", uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class LandingPhonePreviews

/** A tablet or unfolded foldable, in light and dark themes. */
@Preview(name = "light", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(name = "dark", device = "spec:width=1280dp,height=800dp,dpi=240", uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class LandingTabletPreviews

/** A desktop window, in light and dark themes. */
@Preview(name = "light", device = "spec:width=1440dp,height=900dp,dpi=160")
@Preview(name = "dark", device = "spec:width=1440dp,height=900dp,dpi=160", uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class LandingDesktopPreviews

/** A single feature, cropped to its content, in light and dark themes. */
@Preview(name = "light", widthDp = 411)
@Preview(name = "dark", widthDp = 411, uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class LandingFeaturePreviews

/**
 * Loads images from the app's bundled resources, since screenshot tests can't reach the
 * network. See [LandingFixtures] for the paths.
 */
@OptIn(ExperimentalCoilApi::class)
private val landingImagePreviewHandler =
    AsyncImagePreviewHandler { request ->
        val path = request.data as? String
        if (path != null && (path.startsWith("drawable/") || path.startsWith("files/"))) {
            val bytes = Res.readBytes(path)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImage()
        } else {
            ColorImage(color = 0x00000000)
        }
    }

/**
 * The app's theme, following the preview's light or dark mode.
 *
 * @param seedColor the color of the channel's avatar, to theme the content like that
 * channel's chat, or null for the app's default colors.
 */
@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun LandingTheme(
    seedColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides landingImagePreviewHandler) {
        AppTheme(isDarkTheme = isDark) {
            if (seedColor == null) {
                content()
            } else {
                // Same theme as the one DynamicImageColorTheme builds from the avatar.
                DynamicMaterialTheme(
                    seedColor = seedColor,
                    style = PaletteStyle.Expressive,
                    isDark = isDark,
                    animate = false,
                    content = content,
                )
            }
        }
    }
}
