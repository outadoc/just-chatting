package fr.outadoc.justchatting.feature.chat.presentation.ui

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
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalDensity
import coil3.compose.LocalPlatformContext
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.utils.http.toUri
import fr.outadoc.justchatting.utils.presentation.OnLifecycleEvent
import fr.outadoc.justchatting.utils.presentation.areBubblesSupported
import fr.outadoc.justchatting.utils.presentation.isDark
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ChannelChatScreen(
    modifier: Modifier = Modifier,
    userId: String,
    isStandalone: Boolean,
    canNavigateUp: Boolean = true,
    onNavigateUp: () -> Unit = {},
) {
    val viewModel: ChatViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val inputState by viewModel.inputState.collectAsState()

    val preferencesRepository: PreferenceRepository = koinInject()
    val notifier: ChatNotifier = koinInject()

    val prefs by preferencesRepository.currentPreferences.collectAsState(initial = AppPreferences())

    val context = LocalPlatformContext.current
    val density = LocalDensity.current.density

    val user = (state as? ChatViewModel.State.Chatting)?.user

    var isEmotePickerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadChat(userId)
    }

    BackHandler(isEmotePickerOpen) {
        isEmotePickerOpen = false
    }

    val canOpenInBubble: Boolean =
        !isStandalone && areBubblesSupported() && prefs.enableNotifications && notifier.areNotificationsEnabled

    OnLifecycleEvent(
        onPause = {
            if (user != null && canOpenInBubble) {
                notifier.notify(
                    context = context,
                    user = user,
                )
            }
        },
    )

    DynamicImageColorTheme(
        imageUrl = user?.profileImageUrl?.toUri(),
    ) {
        val isDarkTheme = MaterialTheme.colorScheme.isDark
        ChannelChatScreenContent(
            modifier = modifier,
            state = state,
            inputState = inputState,
            showBackButton = !isStandalone && canNavigateUp,
            isEmotePickerOpen = isEmotePickerOpen,
            showTimestamps = prefs.showTimestamps,
            onMessageChange = { textFieldValue ->
                viewModel.onMessageInputChanged(
                    message = textFieldValue.text,
                    selectionRange =
                    IntRange(
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
                if (user != null) {
                    notifier.notify(
                        context = context,
                        user = user,
                    )
                }
            },
            onTriggerAutoComplete = viewModel::onTriggerAutoComplete,
            onSubmit = {
                viewModel.submit(
                    screenDensity = density,
                    isDarkTheme = isDarkTheme,
                )
            },
            onReplyToMessage = viewModel::onReplyToMessage,
            onShowInfoForUserId = viewModel::onShowUserInfo,
            onDismissUserInfo = viewModel::onDismissUserInfo,
            onShowStreamInfo = viewModel::onShowStreamInfo,
            onDismissStreamInfo = viewModel::onDismissStreamInfo,
            onReuseLastMessageClicked = viewModel::onReuseLastMessageClicked,
            onNavigateUp = onNavigateUp,
        )
    }
}
