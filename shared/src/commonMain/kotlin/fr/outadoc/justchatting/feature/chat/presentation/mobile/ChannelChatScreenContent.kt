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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChannelChatScreenContent(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    inputState: ChatViewModel.InputState,
    showBackButton: Boolean = true,
    showBubbleButton: Boolean = true,
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
    onReplyToMessage: (ChatListItem.Message) -> Unit = {},
    onDismissUserInfo: () -> Unit = {},
    onShowInfoForUserId: (String) -> Unit = {},
    onReuseLastMessageClicked: () -> Unit = {},
    onNavigateUp: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val stream = (state as? ChatViewModel.State.Chatting)?.stream
    val user = (state as? ChatViewModel.State.Chatting)?.user

    val inputFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

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
                        onClick = { user?.id?.let(onShowInfoForUserId) },
                        onClickLabel = stringResource(MR.strings.stream_info),
                    ),
                user = user,
                stream = stream,
                onWatchLiveClicked = onWatchLiveClicked,
                onOpenBubbleClicked = onOpenBubbleClicked,
                showBackButton = showBackButton,
                showBubbleButton = showBubbleButton,
                onNavigateUp = onNavigateUp,
            )
        },
        content = { insets ->
            val clipboard = LocalClipboardManager.current
            val haptic = LocalHapticFeedback.current
            val scope = rememberCoroutineScope()
            val snackbarCopiedMessage = stringResource(MR.strings.chat_copiedToClipboard)

            ChatScreen(
                modifier = Modifier.fillMaxSize(),
                state = state,
                showTimestamps = showTimestamps,
                onMessageLongClick = { item ->
                    item.body?.message?.let { rawMessage ->
                        // Copy to clipboard
                        clipboard.setText(AnnotatedString(rawMessage))

                        // Vibrate device
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        // Show "copied to clipboard" confirmation
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = snackbarCopiedMessage,
                                duration = SnackbarDuration.Short,
                            )
                        }
                    }
                },
                onReplyToMessage = onReplyToMessage,
                onShowInfoForUserId = onShowInfoForUserId,
                insets = insets,
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
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
                        message = TextFieldValue(
                            text = inputState.message,
                            selection = TextRange(
                                start = inputState.selectionRange.first,
                                end = inputState.selectionRange.last,
                            ),
                        ),
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
                        canReuseLastMessage = inputState.canReuseLastMessage,
                        onReuseLastMessageClicked = onReuseLastMessageClicked,
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

    val showInfoForUserId: String? =
        (state as? ChatViewModel.State.Chatting)?.showInfoForUserId

    if (showInfoForUserId != null) {
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
                userId = showInfoForUserId,
            )
        }
    }
}

@Preview
@Composable
internal fun ChannelChatScreenLoadingPreview() {
    AppTheme {
        ChannelChatScreenContent(
            state = ChatViewModel.State.Initial,
            inputState = ChatViewModel.InputState(),
            showTimestamps = true,
        )
    }
}
