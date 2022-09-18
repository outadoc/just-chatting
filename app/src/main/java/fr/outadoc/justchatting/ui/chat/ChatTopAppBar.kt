package fr.outadoc.justchatting.ui.chat

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.OpenInNew
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
    onOpenBubbleClicked: () -> Unit,
    onStreamInfoClicked: (User) -> Unit,
    onColorContrastChanged: (isLight: Boolean) -> Unit
) {
    TopAppBar(
        colors = swatch.toolbarColors(onColorContrastChanged),
        title = {
            Column {
                Text(
                    text = user?.display_name ?: channelLogin,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (stream?.title != null) {
                    Text(
                        text = stream.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            logo?.let { logo ->
                Image(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(horizontal = 8.dp),
                    bitmap = logo.asImageBitmap(),
                    contentDescription = null
                )
            }
        },
        actions = {
            user?.let { user ->
                IconButton(onClick = { onWatchLiveClicked(user) }) {
                    Icon(
                        Icons.Default.LiveTv,
                        contentDescription = stringResource(R.string.watch_live)
                    )
                }

                IconButton(onClick = { onOpenBubbleClicked() }) {
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = stringResource(R.string.menu_item_openInBubble)
                    )
                }

                IconButton(onClick = { onStreamInfoClicked(user) }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = stringResource(R.string.stream_info)
                    )
                }
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
