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
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import kotlinx.collections.immutable.toImmutableList

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
                val emotes = state.pickableEmotesWithRecent

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
