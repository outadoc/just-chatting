package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import fr.outadoc.justchatting.feature.home.presentation.ChannelSearchViewModel
import org.koin.androidx.compose.koinViewModel

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

    MainNavigation(
        sizeClass = sizeClass,
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
                    isActive = state.isActive,
                    onQueryChange = viewModel::onQueryChange,
                    onSearchActiveChange = viewModel::onSearchActiveChange,
                    onClear = viewModel::onClear,
                    onDismiss = viewModel::onDismiss,
                )
            }
        },
        content = { insets ->

        },
    )
}