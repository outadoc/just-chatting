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
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.utils.core.createChannelExternalLink
import fr.outadoc.justchatting.utils.ui.canOpenInBubble
import fr.outadoc.justchatting.utils.ui.isDark
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@Composable
internal fun ChannelChatScreen(channelLogin: String) {
    val viewModel: ChatViewModel = getViewModel()
    val state by viewModel.state.collectAsState()
    val inputState by viewModel.inputState.collectAsState()

    val preferencesRepository: PreferenceRepository = koinInject()
    val notifier: ChatNotifier = koinInject()

    val prefs by preferencesRepository.currentPreferences.collectAsState(initial = AppPreferences())

    val context = LocalContext.current
    val density = LocalDensity.current.density
    val uriHandler = LocalUriHandler.current

    val user = (state as? ChatViewModel.State.Chatting)?.user

    var isEmotePickerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(channelLogin) {
        viewModel.loadChat(channelLogin)
    }

    BackHandler(isEmotePickerOpen) {
        isEmotePickerOpen = false
    }

    OnLifecycleEvent(
        onResume = viewModel::onResume,
        onPause = {
            if (user != null) {
                notifier.notify(
                    context = context,
                    user = user,
                )
            }
        },
    )

    // Update task description
    UpdateTaskDescriptionForUser(user)

    val canOpenInBubble: Boolean =
        prefs.enableNotifications &&
            canOpenInBubble() &&
            notifier.areNotificationsEnabled

    MaterialTheme(
        colorScheme = dynamicImageColorScheme(url = user?.profileImageUrl),
    ) {
        val isDarkTheme = MaterialTheme.colorScheme.isDark
        ChannelChatScreenContent(
            state = state,
            inputState = inputState,
            channelLogin = channelLogin,
            isEmotePickerOpen = isEmotePickerOpen,
            showTimestamps = prefs.showTimestamps,
            onWatchLiveClicked = {
                uriHandler.openUri(createChannelExternalLink(channelLogin))
            },
            onMessageChange = { textFieldValue ->
                viewModel.onMessageInputChanged(
                    message = textFieldValue.text,
                    selectionRange = IntRange(
                        start = textFieldValue.selection.start,
                        endInclusive = textFieldValue.selection.end,
                    ),
                )
            },
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
            onTriggerAutoComplete = {
                viewModel.onTriggerAutoComplete()
            },
            onSubmit = {
                viewModel.submit(
                    screenDensity = density,
                    isDarkTheme = isDarkTheme,
                )
            },
            onReplyToMessage = viewModel::onReplyToMessage,
            onDismissUserInfo = viewModel::onDismissUserInfo,
            onShowUserInfoForLogin = viewModel::onShowUserInfo,
        )
    }
}
