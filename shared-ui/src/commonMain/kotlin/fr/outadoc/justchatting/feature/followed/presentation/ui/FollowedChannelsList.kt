package fr.outadoc.justchatting.feature.followed.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.followed.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainTopAppBar
import fr.outadoc.justchatting.feature.shared.presentation.ui.NoContent
import fr.outadoc.justchatting.feature.shared.presentation.ui.ProfileButton
import fr.outadoc.justchatting.feature.shared.presentation.ui.SegmentedListDefaults
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserItemCard
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserItemCardPlaceholder
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.channels
import fr.outadoc.justchatting.shared.internal.timeline_refresh_action_cd
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import fr.outadoc.justchatting.utils.presentation.plus
import fr.outadoc.justchatting.utils.presentation.rememberHasPointingDevice
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FollowedChannelsList(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    onItemClick: (login: String) -> Unit,
) {
    val viewModel: FollowedChannelsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    val hasMouse = rememberHasPointingDevice()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        viewModel.synchronize()
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is FollowedChannelsViewModel.Event.NavigateToChannel -> {
                    onItemClick(event.userId)
                }
            }
        }
    }

    MainNavigation(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        selectedScreen = Screen.Followed,
        onSelectedTabChange = onNavigate,
        topBar = {
            MainTopAppBar(
                title = stringResource(Res.string.channels),
                scrollBehavior = scrollBehavior,
                actions = {
                    if (hasMouse) {
                        AccessibleIconButton(
                            onClick = { viewModel.synchronize() },
                            onClickLabel = stringResource(Res.string.timeline_refresh_action_cd),
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                },
                profileButton = {
                    ProfileButton(onClick = onOpenSettings)
                },
            )
        },
        content = { insets ->
            if (hasMouse) {
                InnerFollowedChannelsList(
                    modifier = Modifier.fillMaxSize(),
                    insets = insets,
                    items = state.data,
                    isRefreshing = state.isLoading,
                    onItemClick = { channel ->
                        viewModel.onChannelClick(channel.user.id)
                    },
                )
            } else {
                val pullToRefreshState = rememberPullToRefreshState()
                PullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    state = pullToRefreshState,
                    isRefreshing = state.isLoading,
                    onRefresh = { viewModel.synchronize() },
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            state = pullToRefreshState,
                            isRefreshing = state.isLoading,
                            modifier =
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = insets.calculateTopPadding()),
                        )
                    },
                ) {
                    InnerFollowedChannelsList(
                        modifier = Modifier.fillMaxSize(),
                        insets = insets,
                        items = state.data,
                        isRefreshing = state.isLoading,
                        onItemClick = { channel ->
                            viewModel.onChannelClick(channel.user.id)
                        },
                    )
                }
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
        verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.ItemSpacing),
        contentPadding =
            insets +
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 16.dp,
                ),
    ) {
        if (items.isEmpty()) {
            if (!isRefreshing) {
                item(key = "_noContent") {
                    NoContent(modifier = Modifier.fillParentMaxSize())
                }
            } else {
                items(PlaceholderCount) { index ->
                    UserItemCardPlaceholder(
                        modifier = Modifier.fillMaxWidth(),
                        shape = SegmentedListDefaults.shape(index = index, count = PlaceholderCount),
                    )
                }
            }
        } else {
            itemsIndexed(
                items = items,
                key = { _, item -> item.user.id },
            ) { index, item ->
                UserItemCard(
                    modifier =
                        Modifier
                            .animateItem()
                            .fillMaxWidth(),
                    displayName = item.user.displayName,
                    profileImageUrl = item.user.profileImageUrl,
                    followedAt = item.followedAt,
                    shape = SegmentedListDefaults.shape(index = index, count = items.size),
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}

private const val PlaceholderCount = 50
