package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.AutoCompleteItem
import fr.outadoc.justchatting.utils.ui.AppTheme

@Preview
@Composable
fun ChatAutoCompleteRowPreview() {
    AppTheme {
        val items = listOf(
            AutoCompleteItem.User(Chatter("BagheraJones")),
            AutoCompleteItem.User(Chatter("HortyUnderscore")),
        )

        ChatAutoCompleteRow(
            onChatterClick = {},
            onEmoteClick = {},
            items = items,
        )
    }
}

@Composable
fun ChatAutoCompleteRow(
    modifier: Modifier = Modifier,
    onChatterClick: (Chatter) -> Unit,
    onEmoteClick: (Emote) -> Unit,
    items: List<AutoCompleteItem>,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = items,
            contentType = { item ->
                when (item) {
                    is AutoCompleteItem.Emote -> 0
                    is AutoCompleteItem.User -> 1
                }
            },
        ) { item ->
            when (item) {
                is AutoCompleteItem.Emote -> {
                    AutoCompleteEmoteItem(
                        onClick = { onEmoteClick(item.emote) },
                        emote = item.emote,
                    )
                }

                is AutoCompleteItem.User -> {
                    AutoCompleteUserItem(
                        onClick = { onChatterClick(item.chatter) },
                        chatter = item.chatter,
                    )
                }
            }
        }
    }
}
