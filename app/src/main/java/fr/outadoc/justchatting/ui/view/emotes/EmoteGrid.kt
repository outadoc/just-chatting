package fr.outadoc.justchatting.ui.view.emotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.ui.chat.EmoteSetItem
import kotlinx.collections.immutable.ImmutableList

@Composable
fun EmoteGrid(
    modifier: Modifier = Modifier,
    emotes: ImmutableList<EmoteSetItem>,
    animateEmotes: Boolean,
    emoteSize: Dp = 32.dp,
    onEmoteClick: (Emote) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    LazyVerticalGrid(
        modifier = modifier,
        contentPadding = contentPadding,
        columns = GridCells.Adaptive(minSize = emoteSize),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(
            items = emotes,
            span = { _, item ->
                when (item) {
                    is EmoteSetItem.Emote -> GridItemSpan(1)
                    is EmoteSetItem.Header -> GridItemSpan(maxLineSpan)
                }
            },
            contentType = { _, item ->
                when (item) {
                    is EmoteSetItem.Emote -> 1
                    is EmoteSetItem.Header -> 2
                }
            }
        ) { index, item ->
            when (item) {
                is EmoteSetItem.Header -> {
                    item.title?.let { title ->
                        EmoteHeader(
                            modifier = Modifier.padding(top = if (index > 0) 8.dp else 0.dp),
                            title = title
                        )
                    }
                }
                is EmoteSetItem.Emote -> {
                    IconButton(
                        onClick = { onEmoteClick(item.emote) }
                    ) {
                        EmoteItem(
                            modifier = Modifier.size(emoteSize),
                            emote = item.emote,
                            animateEmotes = animateEmotes
                        )
                    }
                }
            }
        }
    }
}
