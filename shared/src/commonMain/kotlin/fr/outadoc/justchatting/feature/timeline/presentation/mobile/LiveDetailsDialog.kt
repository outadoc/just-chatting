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
    onChannelClick: (User) -> Unit = {},
    onOpenInBubble: (User) -> Unit = {},
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
        actions = {
            item {
                ContextualButton(
                    onClick = {
                        onChannelClick(user)
                        onDismissRequest()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = null,
                        )
                    },
                    text = "Open chat",
                )
            }

            item {
                ContextualButton(
                    onClick = {
                        onOpenInBubble(user)
                        onDismissRequest()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = null,
                        )
                    },
                    text = "Open in bubble",
                )
            }

            item {
                ContextualButton(
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
            }
        },
    )
}
