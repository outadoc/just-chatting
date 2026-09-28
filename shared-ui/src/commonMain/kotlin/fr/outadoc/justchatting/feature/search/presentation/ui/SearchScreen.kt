package fr.outadoc.justchatting.feature.search.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import fr.outadoc.justchatting.feature.search.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.ui.ProfileButton
import fr.outadoc.justchatting.feature.shared.presentation.ui.SegmentedListDefaults
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserItemCard
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.search_recentChannels_clearAll_action
import fr.outadoc.justchatting.shared.internal.search_recentChannels_header
import fr.outadoc.justchatting.shared.internal.search_recentChannels_remove_action
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
    onOpenSettings: () -> Unit,
    onChannelClick: (userId: String) -> Unit,
) {
    val viewModel: ChannelSearchViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val searchResults = viewModel.pagingData.collectAsLazyPagingItems()

    val hazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        viewModel.onStart()
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is ChannelSearchViewModel.Event.NavigateToChannel -> {
                    onChannelClick(event.userId)
                }
            }
        }
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
                    onChannelClick = viewModel::onChannelClick,
                    onQueryChange = viewModel::onQueryChange,
                    onSearchActiveChange = viewModel::onSearchExpandedChange,
                    onClear = viewModel::onClearSearchBar,
                    onDismiss = viewModel::onDismissSearchBar,
                    collapsedTrailingIcon = {
                        ProfileButton(onClick = onOpenSettings)
                    },
                )
            }
        },
        content = { insets ->
            RecentUsersList(
                modifier = modifier.hazeSource(hazeState),
                insets = insets,
                users = state.recentChannels,
                onChannelClick = { user ->
                    viewModel.onChannelClick(user.id)
                },
                onRemoveChannelClick = viewModel::onRemoveRecentChannel,
                onClearAllClick = viewModel::onClearRecentChannels,
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
    onClearAllClick: () -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.ItemSpacing),
        contentPadding =
            insets +
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
    ) {
        if (users.isNotEmpty()) {
            item(key = "_header") {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.search_recentChannels_header),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    TextButton(onClick = onClearAllClick) {
                        Text(stringResource(Res.string.search_recentChannels_clearAll_action))
                    }
                }
            }
        }

        itemsIndexed(
            items = users,
            key = { _, user -> user.id },
        ) { index, user ->
            UserItemCard(
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
                onClick = { onChannelClick(user) },
                displayName = user.displayName,
                profileImageUrl = user.profileImageUrl,
                shape = SegmentedListDefaults.shape(index = index, count = users.size),
                trailingActions = {
                    AccessibleIconButton(
                        onClick = { onRemoveChannelClick(user) },
                        onClickLabel = stringResource(Res.string.search_recentChannels_remove_action),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        }
    }
}
