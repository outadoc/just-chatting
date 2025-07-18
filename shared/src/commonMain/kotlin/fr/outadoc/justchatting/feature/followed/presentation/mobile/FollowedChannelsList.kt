package fr.outadoc.justchatting.feature.followed.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.followed.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.mobile.NoContent
import fr.outadoc.justchatting.feature.shared.presentation.mobile.Screen
import fr.outadoc.justchatting.feature.shared.presentation.mobile.UserItemCard
import fr.outadoc.justchatting.feature.shared.presentation.mobile.UserItemCardPlaceholder
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.channels
import fr.outadoc.justchatting.shared.timeline_refresh_action_cd
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import fr.outadoc.justchatting.utils.presentation.plus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
)
@Composable
internal fun FollowedChannelsList(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
    onItemClick: (login: String) -> Unit,
) {
    val viewModel: FollowedChannelsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val hazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        viewModel.synchronize()
    }

    MainNavigation(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        selectedScreen = Screen.Followed,
        onSelectedTabChange = onNavigate,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(Color.Transparent),
                modifier = Modifier
                    .hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.regular(),
                    ),
                title = { Text(stringResource(Res.string.channels)) },
                scrollBehavior = scrollBehavior,
                actions = {
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
                },
            )
        },
        content = { insets ->
            InnerFollowedChannelsList(
                modifier = Modifier
                    .haze(hazeState)
                    .fillMaxSize(),
                insets = insets,
                items = state.data,
                isRefreshing = state.isLoading,
                onItemClick = { channel ->
                    onItemClick(channel.user.id)
                },
            )
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
            items(
                items = items,
                key = { item -> item.user.id },
            ) { item ->
                UserItemCard(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth(),
                    displayName = item.user.displayName,
                    profileImageUrl = item.user.profileImageUrl,
                    followedAt = item.followedAt,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}
