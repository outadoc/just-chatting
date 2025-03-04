package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.scroll_down
import fr.outadoc.justchatting.utils.core.filterValuesNotNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChatListContainer(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State.Chatting,
    showTimestamps: Boolean,
    onMessageLongClick: (ChatListItem.Message) -> Unit,
    onReplyToMessage: (ChatListItem.Message) -> Unit,
    onShowInfoForUserId: (String) -> Unit,
    insets: PaddingValues,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val haptic = LocalHapticFeedback.current
    val imeController = LocalSoftwareKeyboardController.current

    var isListAtBottom by remember { mutableStateOf(false) }

    LaunchedEffect(isListAtBottom) {
        // Hide the keyboard when scrolling up
        if (!isListAtBottom) {
            imeController?.hide()
        }
    }

    Box(modifier = modifier) {
        ChatList(
            modifier = Modifier.fillMaxSize(),
            entries = state.chatMessages,
            emotes = state.allEmotesMap,
            cheerEmotes = state.cheerEmotes,
            badges = state.globalBadges.addAll(state.channelBadges),
            removedContent = state.removedContent,
            pronouns = state.pronouns.filterValuesNotNull(),
            richEmbeds = state.richEmbeds,
            showTimestamps = showTimestamps,
            isDisconnected = !state.connectionStatus.isAlive,
            listState = listState,
            onMessageLongClick = onMessageLongClick,
            onReplyToMessage = onReplyToMessage,
            onShowInfoForUserId = onShowInfoForUserId,
            roomState = state.roomState,
            ongoingEvents = state.ongoingEvents,
            appUser = state.appUser,
            onListScrolledToBottom = { isListAtBottom = it },
            insets = insets,
        )

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = !isListAtBottom,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
        ) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(insets),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        listState.scrollToItem(index = 0)
                    }
                },
            ) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = stringResource(Res.string.scroll_down),
                )
            }
        }
    }
}
