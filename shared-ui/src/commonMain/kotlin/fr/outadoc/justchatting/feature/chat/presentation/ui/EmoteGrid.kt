package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun EmoteGrid(
    modifier: Modifier = Modifier,
    emotes: ImmutableList<EmoteSetItem>,
    emoteSize: Dp = 36.dp,
    onEmoteClick: (Emote) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val haptic = LocalHapticFeedback.current
    LazyVerticalGrid(
        modifier = modifier,
        contentPadding = contentPadding,
        columns =
        GridCells.Adaptive(
            minSize = emoteSize + 8.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(
            items = emotes,
            span = { _, item ->
                when (item) {
                    is EmoteSetItem.Emote -> GridItemSpan(currentLineSpan = 1)
                    is EmoteSetItem.Header -> GridItemSpan(maxLineSpan)
                }
            },
            contentType = { _, item ->
                when (item) {
                    is EmoteSetItem.Emote -> 1
                    is EmoteSetItem.Header -> 2
                }
            },
        ) { index, item ->
            when (item) {
                is EmoteSetItem.Header -> {
                    EmoteHeader(
                        modifier =
                        Modifier
                            .padding(top = if (index > 0) 8.dp else 0.dp)
                            .semantics { heading() },
                        header = item,
                    )
                }

                is EmoteSetItem.Emote -> {
                    AccessibleIconButton(
                        modifier = Modifier.aspectRatio(1f),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEmoteClick(item.emote)
                        },
                        onClickLabel = item.emote.name,
                    ) {
                        EmoteItem(
                            modifier = Modifier.size(emoteSize),
                            emote = item.emote,
                        )
                    }
                }
            }
        }
    }
}
