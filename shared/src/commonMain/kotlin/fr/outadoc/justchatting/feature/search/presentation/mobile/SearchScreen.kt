package fr.outadoc.justchatting.feature.search.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import fr.outadoc.justchatting.feature.search.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.mobile.Screen
import fr.outadoc.justchatting.feature.shared.presentation.mobile.UserItemCard
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.search_recentChannels_remove_action
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import fr.outadoc.justchatting.utils.presentation.plus
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
internal fun SearchScreen(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
    onChannelClick: (userId: String) -> Unit,
) {
    val viewModel: ChannelSearchViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val searchResults = viewModel.pagingData.collectAsLazyPagingItems()

    val hazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        viewModel.onStart()
    }

    MainNavigation(
        selectedScreen = Screen.Search,
        onSelectedTabChange = onNavigate,
        topBar = {
            Surface {
                SearchBar(
                    modifier =
                        Modifier
                            .hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.regular(),
                            ),
                    searchResults = searchResults,
                    query = state.query,
                    isSearchExpanded = state.isSearchExpanded,
                    onChannelClick = onChannelClick,
                    onQueryChange = viewModel::onQueryChange,
                    onSearchActiveChange = viewModel::onSearchExpandedChange,
                    onClear = viewModel::onClearSearchBar,
                    onDismiss = viewModel::onDismissSearchBar,
                )
            }
        },
        content = { insets ->
            RecentUsersList(
                modifier = modifier.haze(hazeState),
                insets = insets,
                users = state.recentChannels,
                onChannelClick = { user ->
                    onChannelClick(user.id)
                },
                onRemoveChannelClick = viewModel::onRemoveRecentChannel,
            )
        },
    )
}

@Composable
private fun RecentUsersList(
    modifier: Modifier,
    insets: PaddingValues,
    users: ImmutableList<User>,
    onChannelClick: (User) -> Unit,
    onRemoveChannelClick: (User) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding =
            insets +
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
    ) {
        items(users) { user ->
            UserItemCard(
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
                onClick = { onChannelClick(user) },
                displayName = user.displayName,
                profileImageUrl = user.profileImageUrl,
                trailingActions = {
                    AccessibleIconButton(
                        onClick = { onRemoveChannelClick(user) },
                        onClickLabel = stringResource(Res.string.search_recentChannels_remove_action),
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                        )
                    }
                },
            )
        }
    }
}
