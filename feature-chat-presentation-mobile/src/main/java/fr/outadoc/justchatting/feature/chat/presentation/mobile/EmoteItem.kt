package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.feature.chat.presentation.getBestUrl
import fr.outadoc.justchatting.utils.ui.isDark

@Composable
fun EmoteItem(
    modifier: Modifier = Modifier,
    emote: Emote,
) {
    val density = LocalDensity.current.density
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        contentDescription = emote.name,
        model = remoteImageModel(
            emote.urls.getBestUrl(
                screenDensity = density,
                isDarkTheme = MaterialTheme.colorScheme.isDark,
            ),
        ),
    )
}
