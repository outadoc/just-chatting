package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.ui.BasicUserInfo
import fr.outadoc.justchatting.feature.chat.presentation.ui.StreamInfo
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_openBubble_action
import fr.outadoc.justchatting.shared.chat_open_action
import fr.outadoc.justchatting.shared.watch_live
import fr.outadoc.justchatting.utils.core.createChannelExternalLink
import fr.outadoc.justchatting.utils.presentation.areBubblesSupported
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LiveDetailsDialog(
    modifier: Modifier = Modifier,
    user: User,
    stream: Stream?,
    onDismissRequest: () -> Unit = {},
    onOpenChat: (() -> Unit)? = {},
    onOpenInBubble: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    val preferencesRepository: PreferenceRepository = koinInject()
    val notifier: ChatNotifier = koinInject()

    val prefs by preferencesRepository.currentPreferences.collectAsState(initial = AppPreferences())

    val canOpenInBubble: Boolean =
        areBubblesSupported() && prefs.enableNotifications && notifier.areNotificationsEnabled

    ActionBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        header = {
            BasicUserInfo(user = user)
        },
        content =
        stream?.let {
            { StreamInfo(stream = stream) }
        },
        actions = { padding ->
            if (onOpenChat != null) {
                ContextualButton(
                    contentPadding = padding,
                    onClick = {
                        onOpenChat()
                        onDismissRequest()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = null,
                        )
                    },
                    text = stringResource(Res.string.chat_open_action),
                )
            }

            if (canOpenInBubble) {
                ContextualButton(
                    contentPadding = padding,
                    onClick = {
                        onOpenInBubble()
                        onDismissRequest()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = null,
                        )
                    },
                    text = stringResource(Res.string.chat_openBubble_action),
                )
            }

            ContextualButton(
                contentPadding = padding,
                onClick = {
                    uriHandler.openUri(
                        createChannelExternalLink(user),
                    )
                    onDismissRequest()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                    )
                },
                text = stringResource(Res.string.watch_live),
            )
        },
    )
}
