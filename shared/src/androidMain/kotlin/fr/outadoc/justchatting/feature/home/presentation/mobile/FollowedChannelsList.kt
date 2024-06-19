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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.ui.plus
import kotlinx.datetime.Instant
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FollowedChannelsList(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    selectedTab: Tab,
    onSelectedTabChange: (Tab) -> Unit,
    onItemClick: (login: String) -> Unit,
) {
    val viewModel: FollowedChannelsViewModel = koinViewModel()
    val items: LazyPagingItems<ChannelFollow> = viewModel.pagingData.collectAsLazyPagingItems()
    val isRefreshing = items.loadState.refresh is LoadState.Loading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { items.refresh() },
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    MainNavigation(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        sizeClass = sizeClass,
        selectedTab = selectedTab,
        onSelectedTabChange = onSelectedTabChange,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.channels)) },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { insets ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
                contentAlignment = Alignment.TopCenter,
            ) {
                InnerFollowedChannelsList(
                    modifier = Modifier.fillMaxSize(),
                    insets = insets,
                    items = items,
                    isRefreshing = isRefreshing,
                    onItemClick = { channel ->
                        onItemClick(channel.userLogin)
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
private fun InnerFollowedChannelsList(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    items: LazyPagingItems<ChannelFollow>,
    isRefreshing: Boolean,
    onItemClick: (ChannelFollow) -> Unit,
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
                    UserItemCardPlaceholder(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            items(items.itemCount) { index ->
                val item: ChannelFollow? = items[index]
                if (item != null) {
                    UserItemCard(
                        modifier = Modifier.fillMaxWidth(),
                        displayName = item.userDisplayName,
                        profileImageURL = item.profileImageURL,
                        followedAt = Instant.parse(item.followedAt),
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
}
