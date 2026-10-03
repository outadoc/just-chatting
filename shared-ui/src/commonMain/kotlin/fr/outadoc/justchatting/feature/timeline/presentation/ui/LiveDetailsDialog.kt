package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.SharedChatChannel
import fr.outadoc.justchatting.feature.chat.presentation.ui.BasicUserInfo
import fr.outadoc.justchatting.feature.chat.presentation.ui.ExtraUserInfo
import fr.outadoc.justchatting.feature.chat.presentation.ui.SharedChatCard
import fr.outadoc.justchatting.feature.chat.presentation.ui.StreamInfoCard
import fr.outadoc.justchatting.feature.chat.presentation.ui.UserCreatedAt
import fr.outadoc.justchatting.feature.chat.presentation.ui.createChannelDeeplink
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.chat_openBubble_action
import fr.outadoc.justchatting.shared.internal.chat_open_action
import fr.outadoc.justchatting.shared.internal.watch_live
import fr.outadoc.justchatting.utils.core.TimeZoneProvider
import fr.outadoc.justchatting.utils.core.createChannelExternalLink
import fr.outadoc.justchatting.utils.presentation.areBubblesSupported
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LiveDetailsDialog(
    modifier: Modifier = Modifier,
    user: User,
    stream: Stream?,
    sharedChatChannels: ImmutableList<SharedChatChannel> = persistentListOf(),
    onDismissRequest: () -> Unit = {},
    onOpenChat: (() -> Unit)? = {},
    onOpenInBubble: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    val preferencesRepository: PreferenceRepository = koinInject()
    val notifier: ChatNotifier = koinInject()
    val timeZoneProvider: TimeZoneProvider = koinInject()

    val prefs by preferencesRepository.currentPreferences.collectAsState(initial = AppPreferences())

    val canOpenInBubble: Boolean =
        areBubblesSupported() && prefs.enableNotifications && notifier.areNotificationsEnabled

    ActionBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        header = {
            ChannelDetailsHeader(user = user)
        },
        content = {
            ChannelDetailsContent(
                user = user,
                stream = stream,
                sharedChatChannels = sharedChatChannels,
                timeZone = timeZoneProvider.currentTimeZone,
                onSharedChatChannelClick = { channel ->
                    uriHandler.openUri(
                        createChannelDeeplink(channel.user.id).toString(),
                    )
                    onDismissRequest()
                },
            )
        },
        actions = {
            ChannelDetailsActions(
                onOpenChat =
                    onOpenChat?.let {
                        {
                            onOpenChat()
                            onDismissRequest()
                        }
                    },
                onOpenInBubble =
                    if (canOpenInBubble) {
                        {
                            onOpenInBubble()
                            onDismissRequest()
                        }
                    } else {
                        null
                    },
                onWatchLive = {
                    uriHandler.openUri(
                        createChannelExternalLink(user),
                    )
                    onDismissRequest()
                },
            )
        },
    )
}

/**
 * Actions for a channel's details sheet. Actions with a null callback are hidden.
 */
@Composable
public fun ChannelDetailsActions(
    onOpenChat: (() -> Unit)? = null,
    onOpenInBubble: (() -> Unit)? = null,
    onWatchLive: () -> Unit,
) {
    if (onOpenChat != null) {
        ContextualButton(
            onClick = onOpenChat,
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = null,
                )
            },
            text = stringResource(Res.string.chat_open_action),
        )
    }

    if (onOpenInBubble != null) {
        ContextualButton(
            onClick = onOpenInBubble,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.PictureInPictureAlt,
                    contentDescription = null,
                )
            },
            text = stringResource(Res.string.chat_openBubble_action),
        )
    }

    ContextualButton(
        onClick = onWatchLive,
        icon = {
            Icon(
                imageVector = Icons.Outlined.LiveTv,
                contentDescription = null,
            )
        },
        text = stringResource(Res.string.watch_live),
    )
}

@Composable
public fun ChannelDetailsHeader(
    modifier: Modifier = Modifier,
    user: User,
) {
    BasicUserInfo(
        modifier = modifier,
        user = user,
        subtitle = {
            UserCreatedAt(user = user)
        },
    )
}

@Composable
public fun ChannelDetailsContent(
    modifier: Modifier = Modifier,
    user: User,
    stream: Stream?,
    timeZone: TimeZone,
    sharedChatChannels: ImmutableList<SharedChatChannel> = persistentListOf(),
    onSharedChatChannelClick: ((SharedChatChannel) -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ExtraUserInfo(
            user = user,
            showCreatedAt = false,
        )

        if (stream != null) {
            StreamInfoCard(
                modifier = Modifier.fillMaxWidth(),
                stream = stream,
                timeZone = timeZone,
            )
        }

        if (sharedChatChannels.isNotEmpty()) {
            SharedChatCard(
                modifier = Modifier.fillMaxWidth(),
                channels = sharedChatChannels,
                onChannelClick = onSharedChatChannelClick,
            )
        }
    }
}
