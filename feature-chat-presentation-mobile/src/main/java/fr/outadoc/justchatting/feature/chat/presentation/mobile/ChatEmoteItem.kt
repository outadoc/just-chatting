package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import fr.outadoc.justchatting.feature.chat.data.model.TwitchChatEmote
import fr.outadoc.justchatting.utils.ui.isDark

@Composable
fun ChatEmoteItem(
    modifier: Modifier = Modifier,
    emote: TwitchChatEmote,
    animateEmotes: Boolean,
) {
    val density = LocalDensity.current.density
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        contentDescription = emote.name,
        model = emote.getUrl(
            animate = animateEmotes,
            screenDensity = density,
            isDarkTheme = MaterialTheme.colorScheme.isDark,
        ),
    )
}
