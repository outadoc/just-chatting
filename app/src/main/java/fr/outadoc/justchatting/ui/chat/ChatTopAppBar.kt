package fr.outadoc.justchatting.ui.chat

import android.graphics.Bitmap
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette.Swatch
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.HapticIconButton
import fr.outadoc.justchatting.ui.common.ensureMinimumAlpha
import fr.outadoc.justchatting.ui.common.isLightColor
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
    swatch: Swatch?,
    logo: Bitmap?,
    onWatchLiveClicked: (User) -> Unit,
    onOpenBubbleClicked: (() -> Unit)?,
    onColorContrastChanged: (isLight: Boolean) -> Unit
) {
    var showOverflow by remember { mutableStateOf(false) }
    var showStreamInfo by remember { mutableStateOf(false) }

    val defaultColors = ExpandedTopAppBarPalette(
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    val colors = remember(swatch) { swatch?.toolbarColors() ?: defaultColors }

    LaunchedEffect(colors.contentColor) {
        onColorContrastChanged(colors.contentColor.toArgb().isLightColor)
    }

    ExpandedTopAppBar(
        modifier = modifier,
        colors = colors,
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
                visible = logo != null,
                enter = fadeIn() + slideInHorizontally(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                logo?.let { logo ->
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
            user?.let { user ->
                HapticIconButton(onClick = { onWatchLiveClicked(user) }) {
                    Icon(
                        modifier = Modifier.padding(bottom = 3.dp),
                        imageVector = Icons.Outlined.LiveTv,
                        contentDescription = stringResource(R.string.watch_live)
                    )
                }

                IconButton(onClick = { showOverflow = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.menu_item_showOverflow)
                    )
                }
            }

            DropdownMenu(
                expanded = showOverflow,
                onDismissRequest = { showOverflow = false }
            ) {
                DropdownMenuItem(
                    enabled = onOpenBubbleClicked != null,
                    text = { Text(text = stringResource(R.string.menu_item_openInBubble)) },
                    onClick = {
                        onOpenBubbleClicked?.invoke()
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StreamInfo(
    modifier: Modifier,
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
    colors: ExpandedTopAppBarPalette,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val appBarContainerColor by animateColorAsState(
        targetValue = colors.backgroundColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    Surface(
        color = appBarContainerColor,
        contentColor = colors.contentColor
    ) {
        Column {
            TopAppBar(
                title = title,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = colors.backgroundColor,
                    titleContentColor = colors.contentColor,
                    actionIconContentColor = colors.contentColor
                ),
                scrollBehavior = scrollBehavior
            )

            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                secondRow()
            }
        }
    }
}

data class ExpandedTopAppBarPalette(val backgroundColor: Color, val contentColor: Color)

fun Swatch.toolbarColors(): ExpandedTopAppBarPalette {
    return ExpandedTopAppBarPalette(
        backgroundColor = Color(rgb),
        contentColor = Color(
            ensureMinimumAlpha(
                foreground = titleTextColor,
                background = rgb
            )
        )
    )
}
