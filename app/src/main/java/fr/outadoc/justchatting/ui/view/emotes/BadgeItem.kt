package fr.outadoc.justchatting.ui.view.emotes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import fr.outadoc.justchatting.component.twitch.model.TwitchBadge

@Composable
fun BadgeItem(
    modifier: Modifier = Modifier,
    badge: TwitchBadge
) {
    val density = LocalDensity.current.density
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        model = badge.getUrl(screenDensity = density),
        contentDescription = badge.title
    )
}
