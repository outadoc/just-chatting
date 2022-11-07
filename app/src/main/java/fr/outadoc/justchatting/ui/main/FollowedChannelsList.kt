package fr.outadoc.justchatting.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.ui.follow.channels.FollowedChannelsViewModel
import fr.outadoc.justchatting.ui.streams.LiveStreamCard
import fr.outadoc.justchatting.ui.streams.UserItemCard
import kotlinx.datetime.toInstant
import org.koin.androidx.compose.getViewModel
import plus

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FollowedChannelsList(
    modifier: Modifier = Modifier,
    insets: PaddingValues,
    onItemClick: (Follow) -> Unit
) {
    val viewModel: FollowedChannelsViewModel = getViewModel()
    val items: LazyPagingItems<Follow> = viewModel.pagingData.collectAsLazyPagingItems()
    val isRefreshing = items.loadState.refresh is LoadState.Loading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { items.refresh() }
    )

    Box(
        modifier = modifier.pullRefresh(pullRefreshState),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = insets + PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item: Follow? ->
                if (item != null) {
                    UserItemCard(
                        modifier = Modifier.fillMaxWidth(),
                        displayName = item.toName,
                        profileImageURL = item.profileImageURL,
                        followedAt = item.followedAt?.toInstant(),
                        onClick = { onItemClick(item) }
                    )
                } else {
                    LiveStreamCard(modifier = Modifier.height(64.dp))
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            scale = true
        )
    }
}
