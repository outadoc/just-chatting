package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.utils.core.createChannelExternalLink
import fr.outadoc.justchatting.utils.ui.canOpenInBubble
import fr.outadoc.justchatting.utils.ui.isDark
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun ChannelChatScreen(channelLogin: String) {
    val viewModel: ChatViewModel = getViewModel()
    val state by viewModel.state.collectAsState()
    val inputState by viewModel.inputState.collectAsState()

    val preferencesRepository: PreferenceRepository = get()
    val notifier: ChatNotifier = get()
    val prefs by preferencesRepository.currentPreferences.collectAsState(initial = AppPreferences())

    val context = LocalContext.current
    val density = LocalDensity.current.density
    val uriHandler = LocalUriHandler.current

    val isDarkTheme = MaterialTheme.colorScheme.isDark

    val user = (state as? ChatViewModel.State.Chatting)?.user
    val channelBranding: ChannelBranding = rememberChannelBranding(user)

    var isEmotePickerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(channelLogin) {
        viewModel.loadChat(channelLogin)
    }

    BackHandler(isEmotePickerOpen) {
        isEmotePickerOpen = false
    }

    OnLifecycleEvent(
        onPause = {
            if (user != null) {
                notifier.notify(
                    context = context,
                    user = user,
                )
            }
        },
    )

    val canOpenInBubble = canOpenInBubble()

    ChannelChatScreenContent(
        state = state,
        inputState = inputState,
        channelLogin = channelLogin,
        channelBranding = channelBranding,
        isEmotePickerOpen = isEmotePickerOpen,
        showTimestamps = prefs.showTimestamps,
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
            if (canOpenInBubble && user != null) {
                notifier.notify(
                    context = context,
                    user = user,
                )
            }
        },
        onSubmit = {
            viewModel.submit(
                screenDensity = density,
                isDarkTheme = isDarkTheme,
            )
        },
        onReplyToMessage = viewModel::onReplyToMessage,
    )
}
