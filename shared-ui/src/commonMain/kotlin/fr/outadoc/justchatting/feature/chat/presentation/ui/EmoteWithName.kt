package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.shared.presentation.ui.DetailsListItem
import fr.outadoc.justchatting.feature.shared.presentation.ui.DetailsListItemDefaults

@Composable
internal fun EmoteWithName(
    modifier: Modifier = Modifier,
    emote: Emote,
    onClick: () -> Unit = {},
) {
    DetailsListItem(
        modifier = modifier,
        onClick = onClick,
        leadingContent = {
            EmoteItem(
                modifier =
                    Modifier
                        .height(DetailsListItemDefaults.LeadingContentHeight)
                        .aspectRatio(emote.ratio),
                emote = emote,
            )
        },
        headlineContent = {
            Text(
                text = emote.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}
