package fr.outadoc.justchatting.feature.search.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import fr.outadoc.justchatting.feature.search.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.mobile.Screen
import fr.outadoc.justchatting.feature.shared.presentation.mobile.UserItemCard
import fr.outadoc.justchatting.utils.presentation.plus
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun SearchScreen(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    onNavigate: (Screen) -> Unit,
    onChannelClick: (userId: String) -> Unit,
) {
    val viewModel: ChannelSearchViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val searchResults = viewModel.pagingData.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.onStart()
    }

    MainNavigation(
        selectedScreen = Screen.Search,
        onSelectedTabChange = onNavigate,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            ) {
                SearchBar(
                    onChannelClick = onChannelClick,
                    sizeClass = sizeClass,
                    searchResults = searchResults,
                    query = state.query,
                    isSearchExpanded = state.isSearchExpanded,
                    onQueryChange = viewModel::onQueryChange,
                    onSearchActiveChange = viewModel::onSearchExpandedChange,
                    onClear = viewModel::onClearSearchBar,
                    onDismiss = viewModel::onDismissSearchBar,
                )
            }
        },
        content = { insets ->
            RecentUsersList(
                modifier = modifier,
                insets = insets,
                users = state.recentChannels,
                onChannelClick = onChannelClick,
            )
        },
    )
}

@Composable
private fun RecentUsersList(
    modifier: Modifier,
    insets: PaddingValues,
    users: ImmutableList<User>,
    onChannelClick: (userId: String) -> Unit,
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
        items(users) { user ->
            UserItemCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onChannelClick(user.id) },
                displayName = user.displayName,
                profileImageUrl = user.profileImageUrl,
            )
        }
    }
}
