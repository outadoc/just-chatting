package fr.outadoc.justchatting.feature.followed.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.followed.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.mobile.NoContent
import fr.outadoc.justchatting.feature.shared.presentation.mobile.Screen
import fr.outadoc.justchatting.feature.shared.presentation.mobile.UserItemCard
import fr.outadoc.justchatting.feature.shared.presentation.mobile.UserItemCardPlaceholder
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.plus
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun FollowedChannelsList(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    onNavigate: (Screen) -> Unit,
    onItemClick: (login: String) -> Unit,
) {
    val viewModel: FollowedChannelsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val isRefreshing = state is FollowedChannelsViewModel.State.Loading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = viewModel::refresh,
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    MainNavigation(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        sizeClass = sizeClass,
        selectedScreen = Screen.Followed,
        onSelectedTabChange = onNavigate,
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
                    items = (state as? FollowedChannelsViewModel.State.Content)?.data.orEmpty(),
                    isRefreshing = isRefreshing,
                    onItemClick = { channel ->
                        onItemClick(channel.user.id)
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
    items: List<ChannelFollow>,
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
        if (items.isEmpty()) {
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
            items(items) { item ->
                UserItemCard(
                    modifier = Modifier.fillMaxWidth(),
                    displayName = item.user.displayName,
                    profileImageUrl = item.user.profileImageUrl,
                    followedAt = item.followedAt,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}
