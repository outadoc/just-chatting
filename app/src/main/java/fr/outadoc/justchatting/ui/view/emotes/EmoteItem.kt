package fr.outadoc.justchatting.ui.view.emotes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import fr.outadoc.justchatting.component.twitch.model.chat.Emote
import fr.outadoc.justchatting.utils.ui.isDark

@Composable
fun EmoteItem(
    modifier: Modifier = Modifier,
    emote: Emote,
    animateEmotes: Boolean
) {
    val density = LocalDensity.current.density
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        contentDescription = emote.name,
        model = emote.getUrl(
            animate = animateEmotes,
            screenDensity = density,
            isDarkTheme = MaterialTheme.colorScheme.isDark
        )
    )
}
