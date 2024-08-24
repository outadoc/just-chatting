package fr.outadoc.justchatting.feature.search.presentation.mobile

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.presentation.mobile.NoContent
import fr.outadoc.justchatting.feature.shared.presentation.mobile.UserItemCard
import fr.outadoc.justchatting.feature.shared.presentation.mobile.UserItemCardPlaceholder
import fr.outadoc.justchatting.utils.presentation.plus
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun SearchResultsList(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    searchResults: LazyPagingItems<ChannelSearchResult>,
    onItemClick: (ChannelSearchResult) -> Unit,
) {
    val isRefreshing = searchResults.loadState.refresh is LoadState.Loading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { searchResults.refresh() },
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
            if (searchResults.itemCount == 0) {
                if (isRefreshing) {
                    items(50) {
                        UserItemCardPlaceholder(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    item(key = "_noContent") {
                        NoContent(modifier = Modifier.fillParentMaxSize())
                    }
                }
            } else {
                items(searchResults.itemCount) { index ->
                    val item: ChannelSearchResult? = searchResults[index]
                    if (item != null) {
                        UserItemCard(
                            modifier = Modifier.fillMaxWidth(),
                            displayName = item.user.displayName,
                            profileImageUrl = item.user.profileImageUrl,
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
