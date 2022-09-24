package fr.outadoc.justchatting.ui.chat

import android.app.Activity
import android.app.ActivityManager
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.common.ensureMinimumAlpha
import fr.outadoc.justchatting.ui.common.isLight
import fr.outadoc.justchatting.util.generateAsync
import fr.outadoc.justchatting.util.loadImageToBitmap

data class ChannelBranding(
    val backgroundColor: Color,
    val contentColor: Color,
    val logo: Bitmap?
)

@Composable
fun rememberChannelBranding(
    user: User?,
    fallbackBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    fallbackContentColor: Color = MaterialTheme.colorScheme.onSurface
): ChannelBranding {
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()

    val defaultBranding = ChannelBranding(
        logo = null,
        backgroundColor = fallbackBackgroundColor,
        contentColor = fallbackContentColor
    )

    var branding: ChannelBranding by remember(user) { mutableStateOf(defaultBranding) }

    // Set branding asynchronously
    LaunchedEffect(user?.profileImageUrl) {
        if (user?.profileImageUrl == null) {
            branding = defaultBranding
            return@LaunchedEffect
        }

        val logo = loadImageToBitmap(
            context = context,
            imageUrl = user.profileImageUrl,
            circle = true,
            width = 256,
            height = 256
        )

        val swatch = logo?.let {
            val palette = Palette.Builder(it).generateAsync()
            (palette?.dominantSwatch ?: palette?.dominantSwatch)
        }

        branding = ChannelBranding(
            logo = logo,
            contentColor = swatch?.let {
                Color(
                    ensureMinimumAlpha(
                        foreground = swatch.titleTextColor,
                        background = swatch.rgb
                    )
                )
            } ?: fallbackContentColor,
            backgroundColor = swatch?.let { Color(swatch.rgb) }
                ?: fallbackBackgroundColor
        )
    }

    // Update task description
    LaunchedEffect(user?.displayName, branding.logo) {
        if (user?.displayName != null && branding.logo != null) {
            (context as? Activity)?.setTaskDescription(
                ActivityManager.TaskDescription(user.displayName, branding.logo)
            )
        }
    }

    // Update status bar color
    LaunchedEffect(branding) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !branding.contentColor.isLight
        )
    }

    return branding
}
