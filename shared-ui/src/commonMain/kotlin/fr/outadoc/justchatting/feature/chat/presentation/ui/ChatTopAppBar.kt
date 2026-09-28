package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserAvatar
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.all_goBack
import fr.outadoc.justchatting.shared.internal.stream_info
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import fr.outadoc.justchatting.utils.presentation.formatNumber
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    user: User?,
    stream: Stream?,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    onStreamInfoClicked: () -> Unit,
    showBackButton: Boolean,
    onNavigateUp: () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        colors = colors,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(
                    visible = user != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    UserAvatar(
                        modifier = Modifier.padding(end = 12.dp),
                        profileImageUrl = user?.profileImageUrl,
                    )
                }

                Column {
                    if (user != null) {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    val subtitle: String? =
                        stream?.let {
                            listOfNotNull(
                                stream.category?.name?.takeIf { it.isNotEmpty() },
                                stream.viewerCount.toInt().formatNumber(),
                            ).joinToString(separator = " · ")
                        }

                    AnimatedVisibility(visible = subtitle != null) {
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                AccessibleIconButton(
                    onClick = onNavigateUp,
                    onClickLabel = stringResource(Res.string.all_goBack),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            }
        },
        actions = {
            AccessibleIconButton(
                onClick = { onStreamInfoClicked() },
                onClickLabel = stringResource(Res.string.stream_info),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                )
            }
        },
    )
}
