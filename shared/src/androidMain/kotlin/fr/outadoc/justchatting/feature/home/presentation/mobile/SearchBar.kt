package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import androidx.paging.compose.LazyPagingItems
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult

@Composable
internal fun SearchBar(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    searchResults: LazyPagingItems<ChannelSearchResult>,
    query: String,
    isActive: Boolean,
    onChannelClick: (userId: String) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Talkback focus order sorts based on x and y position before considering z-index. The
    // extra Box with semantics and fillMaxWidth is a workaround to get the search bar to focus
    // before the content.
    Box(
        modifier
            .semantics { isTraversalGroup = true }
            .zIndex(1f)
            .fillMaxWidth(),
    ) {
        SearchScreenBar(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth(),
            sizeClass = sizeClass,
            searchResults = searchResults,
            query = query,
            isActive = isActive,
            onChannelClick = onChannelClick,
            onQueryChange = onQueryChange,
            onSearchActiveChange = onSearchActiveChange,
            onClear = onClear,
            onDismiss = onDismiss,
        )
    }
}
