package fr.outadoc.justchatting.ui.chat

import android.graphics.Bitmap
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
import androidx.palette.graphics.Palette
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.chat.Chatter
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.emotes.EmotePicker
import fr.outadoc.justchatting.util.generateAsync
import fr.outadoc.justchatting.util.loadImageToBitmap
import fr.outadoc.justchatting.util.shortToast

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChannelChatScreen(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    channelLogin: String,
    isEmotePickerOpen: Boolean,
    onChannelLogoLoaded: (User, Bitmap) -> Unit,
    onWatchLiveClicked: (User) -> Unit,
    onOpenBubbleClicked: (() -> Unit)?,
    onColorContrastChanged: (isLight: Boolean) -> Unit,
    onMessageChange: (TextFieldValue) -> Unit,
    onToggleEmotePicker: () -> Unit,
    onEmoteClick: (Emote) -> Unit,
    onChatterClick: (Chatter) -> Unit,
    onClearReplyingTo: () -> Unit,
    onSubmit: () -> Unit,
    onReplyToMessage: (ChatEntry) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val stream = (state as? ChatViewModel.State.Chatting)?.stream
    val user = (state as? ChatViewModel.State.Chatting)?.user

    var logo: Bitmap? by remember { mutableStateOf(null) }
    var swatch: Palette.Swatch? by remember { mutableStateOf(null) }

    LaunchedEffect(user) {
        user?.profileImageUrl ?: return@LaunchedEffect

        logo = loadImageToBitmap(
            context = context,
            imageUrl = user.profileImageUrl,
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
            swatch = swatch,
            logo = logo,
            onWatchLiveClicked = onWatchLiveClicked,
            onOpenBubbleClicked = onOpenBubbleClicked,
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
