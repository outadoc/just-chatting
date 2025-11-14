package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.mobile.BasicUserInfo
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ExtraUserInfo
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.mobile.NoContent
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LiveTimelineContent(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    live: ImmutableList<UserStream>,
    listState: LazyListState,
    onChannelClick: (User) -> Unit,
    onOpenInBubble: (User) -> Unit,
) {
    var showUserDetails: User? by remember { mutableStateOf(null) }

    if (live.isEmpty()) {
        NoContent(
            modifier = modifier
                .padding(insets)
                .fillMaxSize(),
        )
    } else {
        LazyColumn(
            modifier = modifier
                .padding(insets)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                live,
                key = { userStream -> userStream.stream.id },
                contentType = { "stream" },
            ) { userStream ->
                LiveTimelineSegment(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth(),
                    userStream = userStream,
                    onOpenChat = {
                        onChannelClick(userStream.user)
                    },
                    onUserClick = {
                        showUserDetails = userStream.user
                    },
                    onOpenInBubble = {
                        onOpenInBubble(userStream.user)
                    },
                )
            }
        }
    }

    showUserDetails?.let { user ->
        ActionBottomSheet(
            onDismissRequest = { showUserDetails = null },
            header = {
                BasicUserInfo(user = user)
            },
            content = {
                ExtraUserInfo(user = user)
            },
        )
    }
}
