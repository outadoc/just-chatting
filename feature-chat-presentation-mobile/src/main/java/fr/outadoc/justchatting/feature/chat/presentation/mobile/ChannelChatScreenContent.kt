package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ScreenPreviews
import fr.outadoc.justchatting.utils.ui.shortToast

@ScreenPreviews
@Composable
fun ChannelChatScreenLoadingPreview() {
    AppTheme {
        ChannelChatScreenContent(
            state = ChatViewModel.State.Initial,
            inputState = ChatViewModel.InputState(),
            channelLogin = "outadoc",
            channelBranding = rememberChannelBranding(user = null),
            showTimestamps = true,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChannelChatScreenContent(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    inputState: ChatViewModel.InputState,
    channelLogin: String,
    channelBranding: ChannelBranding,
    isEmotePickerOpen: Boolean = false,
    showTimestamps: Boolean,
    onWatchLiveClicked: () -> Unit = {},
    onMessageChange: (TextFieldValue) -> Unit = {},
    onToggleEmotePicker: () -> Unit = {},
    onEmoteClick: (Emote) -> Unit = {},
    onChatterClick: (Chatter) -> Unit = {},
    onClearReplyingTo: () -> Unit = {},
    onOpenBubbleClicked: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onReplyToMessage: (ChatEvent.Message) -> Unit = {},
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

    Scaffold(
        modifier = modifier.then(
            if (!isEmotePickerOpen) {
                Modifier.imePadding()
            } else {
                Modifier
            },
        ),
        topBar = {
            ChatTopAppBar(
                channelLogin = channelLogin,
                user = user,
                stream = stream,
                channelBranding = channelBranding,
                onWatchLiveClicked = onWatchLiveClicked,
                onOpenBubbleClicked = onOpenBubbleClicked,
            )
        },
        content = { insets ->
            ChatScreen(
                modifier = Modifier.fillMaxSize(),
                state = state,
                showTimestamps = showTimestamps,
                onMessageLongClick = { item ->
                    item.data?.message?.let { rawMessage ->
                        clipboard.setText(AnnotatedString(rawMessage))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        context.shortToast(R.string.chat_copiedToClipboard)
                    }
                },
                onReplyToMessage = onReplyToMessage,
                insets = insets,
            )
        },
        bottomBar = {
            Column {
                ChatSlowModeProgress(
                    modifier = Modifier.fillMaxWidth(),
                    state = state,
                )

                Surface(
                    shadowElevation = 2.dp,
                    tonalElevation = 1.dp,
                ) {
                    ChatInput(
                        modifier = Modifier
                            .focusRequester(inputFocusRequester)
                            .padding(8.dp)
                            .then(
                                if (!isEmotePickerOpen) {
                                    Modifier.navigationBarsPadding()
                                } else {
                                    Modifier
                                },
                            )
                            .fillMaxWidth(),
                        canSubmit = state is ChatViewModel.State.Chatting,
                        message = inputState.inputMessage,
                        autoCompleteItems = inputState.autoCompleteItems,
                        replyingTo = inputState.replyingTo,
                        onEmoteClick = onEmoteClick,
                        onChatterClick = onChatterClick,
                        onMessageChange = onMessageChange,
                        onToggleEmotePicker = {
                            if (isEmotePickerOpen) {
                                inputFocusRequester.requestFocus()
                                keyboardController?.show()
                            }
                            onToggleEmotePicker()
                        },
                        onClearReplyingTo = onClearReplyingTo,
                        onSubmit = onSubmit,
                    )
                }

                var imeHeight by remember { mutableStateOf(350.dp) }

                val currentImeHeight = WindowInsets.ime
                    .asPaddingValues()
                    .calculateBottomPadding()

                LaunchedEffect(currentImeHeight) {
                    if (currentImeHeight > imeHeight) {
                        imeHeight = currentImeHeight
                    }
                }

                AnimatedVisibility(
                    visible = isEmotePickerOpen,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imeHeight),
                    ) {
                        EmotePicker(
                            onEmoteClick = onEmoteClick,
                            state = state,
                        )
                    }
                }
            }
        },
    )
}
