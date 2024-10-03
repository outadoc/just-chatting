package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.mobile.BasicUserInfo
import fr.outadoc.justchatting.feature.chat.presentation.mobile.StreamInfo
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.createChannelExternalLink

@Composable
internal fun LiveDetailsDialog(
    modifier: Modifier = Modifier,
    user: User,
    stream: Stream,
    onDismissRequest: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenInBubble: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    ActionBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        header = {
            BasicUserInfo(user = user)
        },
        content = {
            StreamInfo(stream = stream)
        },
        actions = { padding ->
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
                text = stringResource(MR.strings.chat_open_action),
            )

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
                text = stringResource(MR.strings.chat_openBubble_action),
            )

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
                text = stringResource(MR.strings.watch_live),
            )
        },
    )
}
