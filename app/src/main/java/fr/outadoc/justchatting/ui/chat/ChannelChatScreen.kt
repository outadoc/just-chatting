package fr.outadoc.justchatting.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.composepreview.ScreenPreviews
import fr.outadoc.justchatting.composepreview.ThemePreviews
import fr.outadoc.justchatting.model.chat.Chatter
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.emotes.EmotePicker
import fr.outadoc.justchatting.util.shortToast

@ScreenPreviews
@Composable
fun ChannelChatScreenLoadingPreview() {
    Mdc3Theme {
        ChannelChatScreen(
            state = ChatViewModel.State.Initial,
            channelLogin = "outadoc",
            channelBranding = rememberChannelBranding(user = null),
            animateEmotes = false,
            showTimestamps = true
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChannelChatScreen(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    channelLogin: String,
    channelBranding: ChannelBranding,
    isEmotePickerOpen: Boolean = false,
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    onWatchLiveClicked: () -> Unit = {},
    onMessageChange: (TextFieldValue) -> Unit = {},
    onToggleEmotePicker: () -> Unit = {},
    onEmoteClick: (Emote) -> Unit = {},
    onChatterClick: (Chatter) -> Unit = {},
    onClearReplyingTo: () -> Unit = {},
    onOpenBubbleClicked: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onReplyToMessage: (ChatEntry) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val stream = (state as? ChatViewModel.State.Chatting)?.stream
    val user = (state as? ChatViewModel.State.Chatting)?.user

    val inputFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isEmotePickerOpen) {
        if (isEmotePickerOpen) {
            keyboardController?.hide()
        }
    }

    Column(
        modifier = modifier.then(
            if (!isEmotePickerOpen) Modifier.imePadding()
            else Modifier
        ),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ChatTopAppBar(
            channelLogin = channelLogin,
            user = user,
            stream = stream,
            channelBranding = channelBranding,
            onWatchLiveClicked = onWatchLiveClicked,
            onOpenBubbleClicked = onOpenBubbleClicked
        )

        ChatScreen(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = state,
            animateEmotes = animateEmotes,
            showTimestamps = showTimestamps,
            onMessageLongClick = { item ->
                item.data?.message?.let { rawMessage ->
                    clipboard.setText(AnnotatedString(rawMessage))
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    context.shortToast(R.string.chat_copiedToClipboard)
                }
            },
            onReplyToMessage = onReplyToMessage
        )

        ChatSlowModeProgress(
            modifier = Modifier.fillMaxWidth(),
            state = state
        )

        ChatInput(
            modifier = Modifier
                .focusRequester(inputFocusRequester)
                .padding(8.dp)
                .then(
                    if (!isEmotePickerOpen) Modifier.navigationBarsPadding()
                    else Modifier
                )
                .fillMaxWidth(),
            state = state,
            animateEmotes = animateEmotes,
            onMessageChange = onMessageChange,
            onToggleEmotePicker = {
                if (isEmotePickerOpen) {
                    inputFocusRequester.requestFocus()
                    keyboardController?.show()
                }
                onToggleEmotePicker()
            },
            onEmoteClick = onEmoteClick,
            onChatterClick = onChatterClick,
            onClearReplyingTo = onClearReplyingTo,
            onSubmit = onSubmit
        )

        var imeHeight by remember { mutableStateOf(350.dp) }

        val currentImeHeight = WindowInsets.ime
            .asPaddingValues()
            .calculateBottomPadding()

        LaunchedEffect(currentImeHeight) {
            if (currentImeHeight > imeHeight) {
                imeHeight = currentImeHeight
            }
        }

        AnimatedVisibility(visible = isEmotePickerOpen) {
            EmotePicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imeHeight),
                onEmoteClick = onEmoteClick,
                state = state
            )
        }
    }
}
