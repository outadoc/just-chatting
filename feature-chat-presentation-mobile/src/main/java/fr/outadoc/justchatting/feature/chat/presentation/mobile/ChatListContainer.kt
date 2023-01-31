package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatListContainer(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State.Chatting,
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    onMessageLongClick: (ChatEvent.Message) -> Unit,
    onReplyToMessage: (ChatEvent.Message) -> Unit,
    insets: PaddingValues,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val haptic = LocalHapticFeedback.current

    var wasListScrolledByUser by remember { mutableStateOf(false) }

    if (listState.isScrollInProgress) {
        wasListScrolledByUser = true
    }

    LaunchedEffect(state.chatMessages, wasListScrolledByUser) {
        if (!wasListScrolledByUser) {
            listState.scrollToItem(
                index = (state.chatMessages.size - 1).coerceAtLeast(0),
            )
        }
    }

    Box(modifier = modifier) {
        ChatList(
            entries = state.chatMessages,
            emotes = state.allEmotesMap,
            cheerEmotes = state.cheerEmotes,
            badges = state.globalBadges.addAll(state.channelBadges),
            roomState = state.roomState,
            animateEmotes = animateEmotes,
            showTimestamps = showTimestamps,
            isDisconnected = !state.connectionStatus.isAlive,
            listState = listState,
            onMessageLongClick = onMessageLongClick,
            onReplyToMessage = onReplyToMessage,
            appUser = state.appUser,
            insets = insets,
        )

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = wasListScrolledByUser,
        ) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(insets),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        wasListScrolledByUser = false
                        listState.scrollToItem(
                            index = (state.chatMessages.size - 1).coerceAtLeast(0),
                        )
                    }
                },
            ) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = stringResource(R.string.scroll_down),
                )
            }
        }
    }
}
