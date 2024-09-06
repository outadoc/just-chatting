package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.ThemePreviews

@Composable
internal fun ChatRichEmbed(
    modifier: Modifier = Modifier,
    title: String,
    authorName: String,
    thumbnailUrl: String,
    requestUrl: String,
) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = modifier.height(64.dp),
        onClick = { uriHandler.openUri(requestUrl) },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxHeight()
                    .aspectRatio(16 / 9f),
                model = thumbnailUrl,
                contentDescription = null,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                Text(
                    text = stringResource(
                        MR.strings.richEmbed_author_title,
                        authorName,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
internal fun ChatRichEmbedPreview() {
    AppTheme {
        ChatRichEmbed(
            modifier = Modifier.width(640.dp),
            title = "Salut ma cocotte",
            authorName = "Name of a clipper",
            thumbnailUrl = "",
            requestUrl = "",
        )
    }
}

@Composable
internal fun ChatRichEmbed(
    modifier: Modifier = Modifier,
    richEmbed: ChatListItem.RichEmbed,
) {
    ChatRichEmbed(
        modifier = modifier,
        title = richEmbed.title,
        authorName = richEmbed.authorName,
        thumbnailUrl = richEmbed.thumbnailUrl,
        requestUrl = richEmbed.requestUrl,
    )
}
