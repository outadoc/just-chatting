package fr.outadoc.justchatting.ui.chat

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.view.emotes.EmotePicker
import fr.outadoc.justchatting.util.generateAsync
import fr.outadoc.justchatting.util.isDark
import fr.outadoc.justchatting.util.loadImageToBitmap
import fr.outadoc.justchatting.util.shortToast

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChannelChatScreen(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel,
    channelChatViewModel: ChannelChatViewModel,
    channelLogin: String,
    onChannelLogoLoaded: (User, Bitmap) -> Unit,
    onWatchLiveClicked: (User) -> Unit,
    onOpenBubbleClicked: () -> Unit,
    onStreamInfoClicked: (User) -> Unit,
    onColorContrastChanged: (isLight: Boolean) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val state by chatViewModel.state.observeAsState(ChatViewModel.State.Initial)
    val channelState by channelChatViewModel.state.observeAsState(
        ChannelChatViewModel.State.Loading
    )

    var isEmotePickerOpen by remember { mutableStateOf(false) }

    val stream = (channelState as? ChannelChatViewModel.State.Loaded)?.stream
    val user = (channelState as? ChannelChatViewModel.State.Loaded)?.loadedUser

    var logo: Bitmap? by remember { mutableStateOf(null) }
    var swatch: Palette.Swatch? by remember { mutableStateOf(null) }

    LaunchedEffect(user) {
        user?.profile_image_url ?: return@LaunchedEffect

        logo = loadImageToBitmap(
            context = context,
            imageUrl = user.profile_image_url,
            circle = true,
            width = 256,
            height = 256
        )

        swatch = logo?.let {
            val palette = Palette.Builder(it).generateAsync()
            (palette?.dominantSwatch ?: palette?.dominantSwatch)
        }

        logo?.let { logo ->
            onChannelLogoLoaded(user, logo)
        }
    }

    BackHandler(isEmotePickerOpen) {
        isEmotePickerOpen = false
    }

    LaunchedEffect(isEmotePickerOpen) {
        if (isEmotePickerOpen) {
            keyboardController?.hide()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ChatTopAppBar(
            channelLogin = channelLogin,
            user = user,
            stream = stream,
            swatch = swatch,
            logo = logo,
            onWatchLiveClicked = onWatchLiveClicked,
            onOpenBubbleClicked = onOpenBubbleClicked,
            onStreamInfoClicked = onStreamInfoClicked,
            onColorContrastChanged = onColorContrastChanged
        )

        ChatScreen(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = state,
            onMessageLongClick = { item ->
                item.data?.message?.let { rawMessage ->
                    clipboard.setText(AnnotatedString(rawMessage))
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    context.shortToast(R.string.chat_copiedToClipboard)
                }
            },
            onReplyToMessage = chatViewModel::onReplyToMessage
        )

        ChatSlowModeProgress(
            modifier = Modifier.fillMaxWidth(),
            state = state
        )

        val density = LocalDensity.current.density
        val isDarkTheme = MaterialTheme.colorScheme.isDark

        ChatInput(
            modifier = Modifier
                .padding(8.dp)
                .then(
                    if (!isEmotePickerOpen) Modifier.navigationBarsPadding()
                    else Modifier
                )
                .fillMaxWidth(),
            state = state,
            onMessageChange = chatViewModel::onMessageInputChanged,
            onToggleEmotePicker = {
                isEmotePickerOpen = !isEmotePickerOpen
            },
            onEmoteClick = { emote ->
                chatViewModel.appendEmote(emote, autocomplete = true)
            },
            onChatterClick = { chatter ->
                chatViewModel.appendChatter(chatter, autocomplete = true)
            },
            onClearReplyingTo = {
                chatViewModel.onReplyToMessage(null)
            },
            onSubmit = {
                chatViewModel.submit(
                    screenDensity = density,
                    isDarkTheme = isDarkTheme
                )
            }
        )

        var imeHeight by remember { mutableStateOf(EmojiPickerDefaults.DefaultHeight) }

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
                onEmoteClick = { emote ->
                    chatViewModel.appendEmote(emote, autocomplete = false)
                },
                state = state
            )
        }
    }
}