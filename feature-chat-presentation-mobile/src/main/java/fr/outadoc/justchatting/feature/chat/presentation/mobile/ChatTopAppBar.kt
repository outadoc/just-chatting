package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.utils.ui.HapticIconButton
import fr.outadoc.justchatting.utils.ui.canOpenInBubble
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    channelLogin: String,
    user: User?,
    stream: Stream?,
    onWatchLiveClicked: () -> Unit,
    onOpenBubbleClicked: () -> Unit,
) {
    val notifier: ChatNotifier = koinInject()

    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(
                    text = user?.displayName ?: channelLogin,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                AnimatedVisibility(visible = stream?.gameName != null) {
                    stream?.gameName?.let { gameName ->
                        Text(
                            text = gameName,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        navigationIcon = {
            AnimatedVisibility(
                visible = user?.profileImageUrl != null,
                enter = fadeIn() + slideInHorizontally(),
                exit = slideOutHorizontally() + fadeOut(),
            ) {
                user?.profileImageUrl?.let { imageUrl ->
                    Row(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            model = imageUrl,
                            contentDescription = null,
                        )
                    }
                }
            }
        },
        actions = {
            HapticIconButton(onClick = { onWatchLiveClicked() }) {
                Icon(
                    modifier = Modifier.padding(bottom = 3.dp),
                    imageVector = Icons.Outlined.LiveTv,
                    contentDescription = stringResource(R.string.watch_live),
                )
            }

            if (canOpenInBubble() && notifier.supportsBackgroundChatService) {
                HapticIconButton(onClick = { onOpenBubbleClicked() }) {
                    Icon(
                        imageVector = Icons.Default.Compress,
                        contentDescription = stringResource(R.string.menu_item_openInBubble),
                    )
                }
            }
        },
    )
}
