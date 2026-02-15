package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.all_goBack
import fr.outadoc.justchatting.shared.stream_info
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    user: User?,
    stream: Stream?,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    onUserClicked: () -> Unit,
    onStreamInfoClicked: () -> Unit,
    showBackButton: Boolean,
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
                        onClickLabel = stringResource(Res.string.all_goBack),
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
                            modifier =
                            Modifier
                                .size(56.dp)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AsyncImage(
                                modifier =
                                Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable(onClick = onUserClicked)
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
