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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
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
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    user: User?,
    stream: Stream?,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    onWatchLiveClicked: () -> Unit,
    onOpenBubbleClicked: () -> Unit,
    showBackButton: Boolean,
    showBubbleButton: Boolean,
    onNavigateUp: () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        colors = colors,
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
                    AccessibleIconButton(
                        onClick = onNavigateUp,
                        onClickLabel = stringResource(MR.strings.all_goBack),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
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
            AccessibleIconButton(
                onClick = { onWatchLiveClicked() },
                onClickLabel = stringResource(MR.strings.watch_live),
            ) {
                Icon(
                    modifier = Modifier.padding(bottom = 3.dp),
                    imageVector = Icons.Outlined.LiveTv,
                    contentDescription = null,
                )
            }

            if (showBubbleButton) {
                AccessibleIconButton(
                    onClick = { onOpenBubbleClicked() },
                    onClickLabel = stringResource(MR.strings.menu_item_openInBubble),
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureInPictureAlt,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}
