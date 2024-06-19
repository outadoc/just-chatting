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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.feature.home.presentation.FollowedStreamsViewModel
import fr.outadoc.justchatting.utils.ui.plus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Instant
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LiveChannelsList(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    onNavigate: (Screen) -> Unit,
    onItemClick: (login: String) -> Unit,
) {
    val viewModel: FollowedStreamsViewModel = koinViewModel()
    val items: LazyPagingItems<Stream> = viewModel.pagingData.collectAsLazyPagingItems()
    val isRefreshing = items.loadState.refresh is LoadState.Loading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { items.refresh() },
    )

    MainNavigation(
        modifier = modifier,
        sizeClass = sizeClass,
        selectedScreen = Screen.Live,
        onSelectedTabChange = onNavigate,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            ) {
                SearchBar(
                    onChannelClick = onItemClick,
                    sizeClass = sizeClass,
                )
            }
        },
        content = { insets ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
                contentAlignment = Alignment.TopCenter,
            ) {
                InnerLiveChannelsList(
                    modifier = Modifier.fillMaxSize(),
                    insets = insets,
                    items = items,
                    isRefreshing = isRefreshing,
                    onItemClick = { stream ->
                        onItemClick(stream.userLogin)
                    },
                )

                PullRefreshIndicator(
                    modifier = Modifier.padding(insets),
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    scale = true,
                )
            }
        },
    )
}

@Composable
private fun InnerLiveChannelsList(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    items: LazyPagingItems<Stream>,
    isRefreshing: Boolean,
    onItemClick: (Stream) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = insets + PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp,
        ),
    ) {
        if (items.itemCount == 0) {
            if (!isRefreshing) {
                item(key = "_noContent") {
                    NoContent(modifier = Modifier.fillParentMaxSize())
                }
            } else {
                items(50) {
                    LiveStreamCardPlaceholder(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            items(items.itemCount) { index ->
                val item: Stream? = items[index]
                if (item != null) {
                    LiveStreamCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = item.title,
                        userName = item.userName,
                        viewerCount = item.viewerCount,
                        gameName = item.gameName,
                        startedAt = Instant.parse(item.startedAt),
                        profileImageURL = item.profileImageURL,
                        tags = item.tags.toImmutableList(),
                        onClick = { onItemClick(item) },
                    )
                } else {
                    LiveStreamCardPlaceholder(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
