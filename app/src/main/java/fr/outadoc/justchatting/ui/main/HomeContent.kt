package fr.outadoc.justchatting.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.model.helix.stream.Stream

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    onStreamClick: (Stream) -> Unit,
    onFollowClick: (Follow) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    Column(modifier) {
        TabRow(
            selectedTabIndex = selectedTabIndex
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text(stringResource(R.string.live)) }
            )

            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text(stringResource(R.string.channels)) }
            )
        }

        Crossfade(targetState = selectedTabIndex) {
            when (selectedTabIndex) {
                0 -> LiveChannelsList(insets = insets, onItemClick = onStreamClick)
                1 -> FollowedChannelsList(insets = insets, onItemClick = onFollowClick)
                else -> {}
            }
        }
    }
}
