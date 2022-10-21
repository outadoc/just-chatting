package fr.outadoc.justchatting.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.composepreview.ThemePreviews
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.HapticIconButton
import fr.outadoc.justchatting.util.formatNumber
import fr.outadoc.justchatting.util.formatTime
import fr.outadoc.justchatting.util.formatTimestamp
import kotlinx.datetime.toInstant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    channelLogin: String,
    user: User?,
    stream: Stream?,
    channelBranding: ChannelBranding,
    onWatchLiveClicked: () -> Unit,
    onOpenBubbleClicked: () -> Unit
) {
    var showOverflow by remember { mutableStateOf(false) }
    var showStreamInfo by remember { mutableStateOf(false) }

    ExpandedTopAppBar(
        modifier = modifier,
        contentColor = channelBranding.contentColor,
        backgroundColor = channelBranding.backgroundColor,
        title = {
            Column {
                Text(
                    text = user?.displayName ?: channelLogin,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                AnimatedVisibility(visible = stream?.gameName != null) {
                    stream?.gameName?.let { gameName ->
                        Text(
                            text = gameName,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        navigationIcon = {
            AnimatedVisibility(
                visible = channelBranding.logo != null,
                enter = fadeIn() + slideInHorizontally(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                channelBranding.logo?.let { logo ->
                    IconButton(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(horizontal = 8.dp),
                        onClick = { showStreamInfo = !showStreamInfo }
                    ) {
                        Image(
                            bitmap = logo.asImageBitmap(),
                            contentDescription = stringResource(R.string.stream_info)
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
                    contentDescription = stringResource(R.string.watch_live)
                )
            }

            IconButton(onClick = { showOverflow = !showOverflow }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.menu_item_showOverflow)
                )
            }

            DropdownMenu(
                expanded = showOverflow,
                onDismissRequest = { showOverflow = false }
            ) {
                DropdownMenuItem(
                    enabled = canOpenInBubble(),
                    text = { Text(text = stringResource(R.string.menu_item_openInBubble)) },
                    onClick = {
                        onOpenBubbleClicked()
                        showOverflow = false
                    }
                )
            }
        },
        secondRow = {
            AnimatedVisibility(
                visible = showStreamInfo,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                StreamInfo(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    user = user,
                    stream = stream
                )
            }
        }
    )
}

@ThemePreviews
@Composable
fun StreamInfoPreviewFull() {
    Mdc3Theme {
        StreamInfo(
            user = User(
                id = "",
                login = "",
                displayName = "",
                followersCount = 50,
                createdAt = "2022-01-01T00:00:00.00Z"
            ),
            stream = Stream(
                title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
                gameName = "",
                startedAt = "2022-09-01T00:00:00.00Z",
                viewerCount = 10_000
            )
        )
    }
}

@ThemePreviews
@Composable
fun StreamInfoPreviewOffline() {
    Mdc3Theme {
        StreamInfo(
            user = User(
                id = "",
                login = "",
                displayName = "",
                followersCount = 50,
                createdAt = "2022-01-01T00:00:00.00Z"
            ),
            stream = null
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StreamInfo(
    modifier: Modifier = Modifier,
    user: User?,
    stream: Stream?
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (stream?.title != null) {
            Text(text = stream.title)
        }

        if (stream?.viewerCount != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.viewers,
                        stream.viewerCount,
                        stream.viewerCount.formatNumber()
                    )
                )
            }
        }

        val startedAt = stream?.startedAt?.toInstant()?.formatTimestamp()
        if (startedAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    imageVector = Icons.Default.Start,
                    contentDescription = null
                )
                Text(text = stringResource(R.string.uptime, startedAt))
            }
        }

        if (user?.followersCount != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null
                )
                Text(
                    text = stringResource(
                        R.string.followers,
                        user.followersCount.formatNumber()
                    )
                )
            }
        }

        val createdAt = user?.createdAt?.toInstant()?.formatTime()
        if (createdAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    imageVector = Icons.Default.Cake,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.created_at, createdAt)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    secondRow: @Composable () -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    backgroundColor: Color,
    contentColor: Color,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val appBarContainerColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    Surface(
        modifier = modifier,
        color = appBarContainerColor,
        contentColor = contentColor
    ) {
        Column {
            TopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = contentColor,
                    actionIconContentColor = contentColor
                ),
                scrollBehavior = scrollBehavior
            )

            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                secondRow()
            }
        }
    }
}
