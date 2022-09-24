package fr.outadoc.justchatting.ui.chat

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
    onStreamInfoClicked: (User) -> Unit,
    onColorContrastChanged: (isLight: Boolean) -> Unit
) {
    var showOverflow by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        colors = swatch.toolbarColors(onColorContrastChanged),
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
                        onClick = { user?.let { onStreamInfoClicked(it) } }
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
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Swatch?.toolbarColors(
    onColorContrastChanged: (isLight: Boolean) -> Unit
): TopAppBarColors {
    if (this == null) return TopAppBarDefaults.smallTopAppBarColors()

    val backgroundColor = rgb
    val textColor = ensureMinimumAlpha(
        foreground = titleTextColor,
        background = backgroundColor
    )

    LaunchedEffect(textColor) {
        onColorContrastChanged(textColor.isLightColor)
    }

    return TopAppBarDefaults.smallTopAppBarColors(
        containerColor = Color(backgroundColor),
        titleContentColor = Color(textColor),
        actionIconContentColor = Color(textColor)
    )
}
