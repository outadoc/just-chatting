package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearch
import fr.outadoc.justchatting.feature.home.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.utils.ui.plus
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchResultsList(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    viewModel: ChannelSearchViewModel,
    onItemClick: (ChannelSearch) -> Unit,
) {
    val items: LazyPagingItems<ChannelSearch> = viewModel.pagingData.collectAsLazyPagingItems()
    val state by viewModel.state.collectAsState()

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
            if (state.query.isNotEmpty() && items.itemCount == 0) {
                if (!isRefreshing) {
                    item(key = "_noContent") {
                        NoContent(modifier = Modifier.fillParentMaxSize())
                    }
                } else {
                    items(50) {
                        UserItemCardPlaceholder(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else {
                items(items.itemCount) { index ->
                    val item: ChannelSearch? = items[index]
                    if (item != null) {
                        UserItemCard(
                            modifier = Modifier.fillMaxWidth(),
                            displayName = item.broadcasterDisplayName,
                            profileImageURL = item.profileImageUrl,
                            tags = item.tags.toImmutableList(),
                            onClick = { onItemClick(item) },
                        )
                    } else {
                        UserItemCardPlaceholder(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.padding(insets),
            refreshing = isRefreshing,
            state = pullRefreshState,
            scale = true,
        )
    }
}
