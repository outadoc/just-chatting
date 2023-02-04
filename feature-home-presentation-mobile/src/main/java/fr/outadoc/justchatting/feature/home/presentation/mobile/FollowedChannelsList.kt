package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import fr.outadoc.justchatting.component.chatapi.domain.model.Follow
import fr.outadoc.justchatting.feature.home.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.utils.ui.plus
import kotlinx.datetime.toInstant
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FollowedChannelsList(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    onItemClick: (Follow) -> Unit,
) {
    val viewModel: FollowedChannelsViewModel = getViewModel()
    val items: LazyPagingItems<Follow> = viewModel.pagingData.collectAsLazyPagingItems()
    val isRefreshing = items.loadState.refresh is LoadState.Loading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { items.refresh() },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = insets + PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!isRefreshing && items.itemCount == 0) {
                item(key = "_noContent") {
                    NoContent(modifier = Modifier.fillParentMaxSize())
                }
            } else {
                items(items) { item: Follow? ->
                    if (item != null) {
                        UserItemCard(
                            modifier = Modifier.fillMaxWidth(),
                            displayName = item.toName,
                            profileImageURL = item.profileImageURL,
                            followedAt = item.followedAt?.toInstant(),
                            onClick = { onItemClick(item) },
                        )
                    } else {
                        UserItemCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .placeholder(
                                    visible = true,
                                    shape = CardDefaults.shape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    highlight = PlaceholderHighlight.shimmer(),
                                ),
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            scale = true,
        )
    }
}
