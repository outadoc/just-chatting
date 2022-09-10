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
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.util.isDark

@Composable
fun EmoteItem(
    modifier: Modifier = Modifier,
    emote: Emote,
    animateEmotes: Boolean
) {
    val density = LocalDensity.current.density
    SubcomposeAsyncImage(
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        model = emote.getUrl(
            animate = animateEmotes,
            screenDensity = density,
            isDarkTheme = MaterialTheme.colorScheme.isDark
        ),
        contentDescription = emote.name,
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
