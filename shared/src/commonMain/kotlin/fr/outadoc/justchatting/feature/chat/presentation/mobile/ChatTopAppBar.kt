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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.HapticIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    user: User?,
    stream: Stream?,
    onWatchLiveClicked: () -> Unit,
    onOpenBubbleClicked: () -> Unit,
    showBackButton: Boolean,
    showBubbleButton: Boolean,
    onNavigateUp: () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                AnimatedVisibility(visible = user != null) {
                    if (user != null) {
                        Text(
                            text = user.displayName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                AnimatedVisibility(visible = stream?.category != null) {
                    stream?.category?.let { category ->
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        navigationIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showBackButton) {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(MR.strings.all_goBack),
                        )
                    }
                }

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
            }
        },
        actions = {
            HapticIconButton(onClick = { onWatchLiveClicked() }) {
                Icon(
                    modifier = Modifier.padding(bottom = 3.dp),
                    imageVector = Icons.Outlined.LiveTv,
                    contentDescription = stringResource(MR.strings.watch_live),
                )
            }

            if (showBubbleButton) {
                HapticIconButton(onClick = { onOpenBubbleClicked() }) {
                    Icon(
                        imageVector = Icons.Default.PictureInPictureAlt,
                        contentDescription = stringResource(MR.strings.menu_item_openInBubble),
                    )
                }
            }
        },
    )
}
