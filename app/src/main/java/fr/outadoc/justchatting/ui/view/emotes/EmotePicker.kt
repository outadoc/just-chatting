package fr.outadoc.justchatting.ui.view.emotes

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.ui.chat.ChatViewModel
import org.koin.androidx.compose.get

@Composable
fun EmotePicker(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    chatPreferencesRepository: ChatPreferencesRepository = get(),
    onEmoteClick: (Emote) -> Unit
) {
    val animateEmotes by chatPreferencesRepository.animateEmotes.collectAsState(initial = true)

    var selectedTab by remember { mutableStateOf(EmoteTab.RECENT) }

    when (state) {
        ChatViewModel.State.Initial -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        is ChatViewModel.State.Chatting -> {
            Column {
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    EmoteTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            text = { Text(stringResource(tab.stringRes)) },
                            onClick = { selectedTab = tab }
                        )
                    }
                }

                Crossfade(targetState = selectedTab) { tab ->
                    val emotes = when (tab) {
                        EmoteTab.RECENT -> state.recentEmotes
                        EmoteTab.TWITCH -> state.twitchEmotes
                        EmoteTab.OTHERS -> state.otherEmotes
                    }

                    if (emotes.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        EmoteGrid(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            emotes = emotes.toList(),
                            animateEmotes = animateEmotes,
                            onEmoteClick = onEmoteClick
                        )
                    }
                }
            }
        }
    }
}
