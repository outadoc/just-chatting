package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.AutoCompleteItem
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.utils.presentation.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
internal fun ChatAutoCompleteRowPreview() {
    AppTheme {
        val items =
            listOf(
                AutoCompleteItem.User(
                    Chatter(
                        id = "1",
                        displayName = "BagheraJones",
                        login = "bagherajones",
                    ),
                ),
                AutoCompleteItem.User(
                    Chatter(
                        id = "2",
                        displayName = "HortyUnderscore",
                        login = "hortyunderscore",
                    ),
                ),
            )

        ChatAutoCompleteRow(
            onChatterClick = {},
            onEmoteClick = {},
            items = items,
        )
    }
}

@Composable
internal fun ChatAutoCompleteRow(
    modifier: Modifier = Modifier,
    onChatterClick: (Chatter) -> Unit,
    onEmoteClick: (Emote) -> Unit,
    items: List<AutoCompleteItem>,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val listState = rememberLazyListState()

    // Scroll back to start when items change
    LaunchedEffect(items) {
        listState.scrollToItem(0)
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        horizontalArrangement =
        Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.Start,
        ),
        contentPadding = contentPadding,
    ) {
        items(
            items = items,
            contentType = { item ->
                when (item) {
                    is AutoCompleteItem.Emote -> 0
                    is AutoCompleteItem.User -> 1
                }
            },
            key = { item ->
                when (item) {
                    is AutoCompleteItem.Emote -> item.emote.name
                    is AutoCompleteItem.User -> item.chatter.id
                }
            },
        ) { item ->
            Box(modifier = Modifier.animateItem()) {
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
}
