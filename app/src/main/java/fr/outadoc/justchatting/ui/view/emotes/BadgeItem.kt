package fr.outadoc.justchatting.ui.view.emotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil.compose.SubcomposeAsyncImage
import fr.outadoc.justchatting.model.chat.TwitchBadge

@Composable
fun BadgeItem(
    modifier: Modifier = Modifier,
    badge: TwitchBadge
) {
    val density = LocalDensity.current.density
    SubcomposeAsyncImage(
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        model = badge.getUrl(screenDensity = density),
        contentDescription = badge.title,
        loading = {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
            )
        }
    )
}
