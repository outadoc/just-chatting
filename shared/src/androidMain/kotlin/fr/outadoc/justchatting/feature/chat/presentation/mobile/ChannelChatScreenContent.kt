package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
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
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.shared.MR
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
    isEmotePickerOpen: Boolean = false,
    showTimestamps: Boolean,
    onWatchLiveClicked: () -> Unit = {},
    onMessageChange: (TextFieldValue) -> Unit = {},
    onToggleEmotePicker: () -> Unit = {},
    onEmoteClick: (Emote) -> Unit = {},
    onChatterClick: (Chatter) -> Unit = {},
    onClearReplyingTo: () -> Unit = {},
    onOpenBubbleClicked: () -> Unit = {},
    onTriggerAutoComplete: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onReplyToMessage: (ChatEvent.Message) -> Unit = {},
    onDismissUserInfo: () -> Unit = {},
    onShowUserInfoForLogin: (String) -> Unit = {},
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
                modifier = Modifier
                    .clickable(
                        onClick = { onShowUserInfoForLogin(channelLogin) },
                        onClickLabel = stringResource(MR.strings.stream_info),
                    ),
                channelLogin = channelLogin,
                user = user,
                stream = stream,
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
                    item.body?.message?.let { rawMessage ->
                        clipboard.setText(AnnotatedString(rawMessage))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        context.shortToast(MR.strings.chat_copiedToClipboard.resourceId)
                    }
                },
                onReplyToMessage = onReplyToMessage,
                onShowUserInfoForLogin = onShowUserInfoForLogin,
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
                            .then(
                                if (!isEmotePickerOpen) {
                                    Modifier.navigationBarsPadding()
                                } else {
                                    Modifier
                                },
                            )
                            .fillMaxWidth(),
                        isSubmitVisible = state is ChatViewModel.State.Chatting,
                        isSubmitEnabled = state is ChatViewModel.State.Chatting && !state.connectionStatus.preventSendingMessages,
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
                        onTriggerAutoComplete = onTriggerAutoComplete,
                        onClearReplyingTo = onClearReplyingTo,
                        onSubmit = onSubmit,
                        contentPadding = 8.dp,
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
                            state = state,
                            onEmoteClick = onEmoteClick,
                        )
                    }
                }
            }
        },
    )

    val userInfoBottomSheetState = rememberModalBottomSheetState()

    val showInfoForUserLogin: String? =
        (state as? ChatViewModel.State.Chatting)?.showInfoForUserLogin

    if (showInfoForUserLogin != null) {
        ModalBottomSheet(
            onDismissRequest = { onDismissUserInfo() },
            sheetState = userInfoBottomSheetState,
        ) {
            StreamAndUserInfoScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 24.dp,
                    ),
                userLogin = showInfoForUserLogin,
            )
        }
    }
}
