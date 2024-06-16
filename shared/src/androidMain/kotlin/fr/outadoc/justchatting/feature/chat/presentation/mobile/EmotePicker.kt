package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.feature.chat.data.emotes.EmoteSetItem
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_header_recent
import fr.outadoc.justchatting.utils.core.flatListOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmotePicker(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    onEmoteClick: (Emote) -> Unit,
) {
    when (state) {
        is ChatViewModel.State.Failed -> {}
        is ChatViewModel.State.Initial -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }

        is ChatViewModel.State.Chatting -> {
            Column(modifier = modifier) {
                val emotes = state.getPickableEmotesWithRecent()

                if (emotes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    EmoteGrid(
                        modifier = Modifier.fillMaxSize(),
                        emotes = emotes.toImmutableList(),
                        onEmoteClick = onEmoteClick,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp + WindowInsets.navigationBars
                                .asPaddingValues()
                                .calculateBottomPadding(),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatViewModel.State.Chatting.getPickableEmotesWithRecent(): ImmutableList<EmoteSetItem> =
    flatListOf(
        EmoteSetItem.Header(
            title = stringResource(Res.string.chat_header_recent),
            source = null,
        ),
        recentEmotes
            .filter { recentEmote -> recentEmote.name in allEmotesMap }
            .map { recentEmote -> EmoteSetItem.Emote(recentEmote) },
    )
        .plus(pickableEmotes)
        .toImmutableList()
