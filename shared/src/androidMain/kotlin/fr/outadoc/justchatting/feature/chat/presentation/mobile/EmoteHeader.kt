package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.icerock.moko.resources.compose.localized
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem

@Composable
internal fun EmoteHeader(
    modifier: Modifier = Modifier,
    header: EmoteSetItem.Header,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        header.iconUrl?.let { url ->
            AsyncImage(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Fit,
                model = remoteImageModel(url),
                contentDescription = null,
            )
        }

        Column {
            header.title?.let { title ->
                Text(
                    text = title.localized(),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            header.source?.let { source ->
                Text(
                    text = source.localized(),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
