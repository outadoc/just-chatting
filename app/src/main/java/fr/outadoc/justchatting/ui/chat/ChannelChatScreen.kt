package fr.outadoc.justchatting.ui.chat

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.composepreview.ScreenPreviews
import fr.outadoc.justchatting.model.chat.Chatter
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.emotes.EmotePicker
import fr.outadoc.justchatting.util.createChannelDeeplink
import fr.outadoc.justchatting.util.createChannelExternalLink
import fr.outadoc.justchatting.util.isDark
import fr.outadoc.justchatting.util.shortToast
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun ChannelChatScreen(channelLogin: String) {
    val viewModel: ChatViewModel = getViewModel()
    val state by viewModel.state.collectAsState()
    val inputState by viewModel.inputState.collectAsState()

    val chatPreferencesRepository: ChatPreferencesRepository = get()

    val animateEmotes by chatPreferencesRepository.animateEmotes.collectAsState(initial = true)
    val showTimestamps by chatPreferencesRepository.showTimestamps.collectAsState(initial = false)

    val context = LocalContext.current
    val density = LocalDensity.current.density
    val uriHandler = LocalUriHandler.current

    val isDarkTheme = MaterialTheme.colorScheme.isDark

    val hostModeState = (state as? ChatViewModel.State.Chatting)?.hostModeState
    val user = (state as? ChatViewModel.State.Chatting)?.user

    val channelBranding: ChannelBranding = rememberChannelBranding(user)

    LaunchedEffect(hostModeState) {
        val targetUri = hostModeState?.targetChannelLogin?.createChannelDeeplink()
        if (targetUri != null && hostModeState.viewerCount != null) {
            // The broadcaster just launched a raid to another channel,
            // so let's go there as well!
            uriHandler.openUri(targetUri.toString())
        }
    }

    var isEmotePickerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(channelLogin) {
        viewModel.loadChat(channelLogin)
    }

    BackHandler(isEmotePickerOpen) {
        isEmotePickerOpen = false
    }

    OnLifecycleEvent(
        onPause = {
            if (user != null && channelBranding.logo != null) {
                ChatNotificationUtils.configureChatBubbles(
                    context = context,
                    channel = user,
                    channelLogo = channelBranding.logo
                )
            }
        }
    )

    val canOpenInBubble = canOpenInBubble()

    ChannelChatScreen(
        state = state,
        inputState = inputState,
        channelLogin = channelLogin,
        channelBranding = channelBranding,
        isEmotePickerOpen = isEmotePickerOpen,
        animateEmotes = animateEmotes,
        showTimestamps = showTimestamps,
        onWatchLiveClicked = {
            uriHandler.openUri(channelLogin.createChannelExternalLink().toString())
        },
        onMessageChange = viewModel::onMessageInputChanged,
        onToggleEmotePicker = {
            isEmotePickerOpen = !isEmotePickerOpen
        },
        onEmoteClick = { emote ->
            viewModel.appendEmote(emote, autocomplete = true)
        },
        onChatterClick = { chatter ->
            viewModel.appendChatter(chatter, autocomplete = true)
        },
        onClearReplyingTo = {
            viewModel.onReplyToMessage(null)
        },
        onOpenBubbleClicked = {
            if (canOpenInBubble && user != null && channelBranding.logo != null) {
                ChatNotificationUtils.configureChatBubbles(
                    context = context,
                    channel = user,
                    channelLogo = channelBranding.logo
                )
            }
        },
        onSubmit = {
            viewModel.submit(
                screenDensity = density,
                isDarkTheme = isDarkTheme
            )
        },
        onReplyToMessage = viewModel::onReplyToMessage
    )
}

@ScreenPreviews
@Composable
fun ChannelChatScreenLoadingPreview() {
    Mdc3Theme {
        ChannelChatScreen(
            state = ChatViewModel.State.Initial,
            inputState = ChatViewModel.InputState(),
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
    inputState: ChatViewModel.InputState,
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
            inputState = inputState,
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
