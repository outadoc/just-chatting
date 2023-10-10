package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.chat.presentation.getBestUrl
import fr.outadoc.justchatting.utils.ui.isDark

@Composable
fun BadgeItem(
    modifier: Modifier = Modifier,
    badge: TwitchBadge,
) {
    val density = LocalDensity.current.density
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        model = remoteImageModel(
            badge.urls.getBestUrl(
                screenDensity = density,
                isDarkTheme = MaterialTheme.colorScheme.isDark,
            ),
        ),
        contentDescription = badge.title,
    )
}
