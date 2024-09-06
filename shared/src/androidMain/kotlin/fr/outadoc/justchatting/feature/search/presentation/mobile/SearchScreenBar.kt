package fr.outadoc.justchatting.feature.search.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.HapticIconButton

@Composable
internal fun SearchScreenBar(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    searchResults: LazyPagingItems<ChannelSearchResult>,
    query: String,
    isSearchExpanded: Boolean,
    onChannelClick: (userId: String) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onClearSearchBar: () -> Unit,
    onDismissSearchBar: () -> Unit,
) {
    when (sizeClass.heightSizeClass) {
        WindowHeightSizeClass.Compact,
        WindowHeightSizeClass.Medium,
        -> {
            FullHeightSearchBar(
                modifier = modifier,
                searchResults = searchResults,
                query = query,
                isSearchExpanded = isSearchExpanded,
                onChannelClick = onChannelClick,
                onQueryChange = onQueryChange,
                onSearchExpandedChange = onSearchActiveChange,
                onClearSearchBar = onClearSearchBar,
                onDismissSearchBar = onDismissSearchBar,
            )
        }

        WindowHeightSizeClass.Expanded -> {
            CompactSearchBar(
                modifier = modifier,
                searchResults = searchResults,
                query = query,
                isSearchExpanded = isSearchExpanded,
                onChannelClick = onChannelClick,
                onQueryChange = onQueryChange,
                onSearchExpandedChange = onSearchActiveChange,
                onClearSearchBar = onClearSearchBar,
                onDismissSearchBar = onDismissSearchBar,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CompactSearchBar(
    modifier: Modifier,
    query: String,
    isSearchExpanded: Boolean,
    searchResults: LazyPagingItems<ChannelSearchResult>,
    onChannelClick: (userId: String) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchExpandedChange: (Boolean) -> Unit,
    onClearSearchBar: () -> Unit,
    onDismissSearchBar: () -> Unit,
) {
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = isSearchExpanded,
                onExpandedChange = onSearchExpandedChange,
                placeholder = { Text(stringResource(MR.strings.search_hint)) },
                leadingIcon = {
                    Crossfade(
                        targetState = isSearchExpanded,
                        label = "search leading icon",
                    ) { isActive ->
                        HapticIconButton(
                            onClick = onDismissSearchBar,
                            enabled = isActive,
                        ) {
                            if (isActive) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(MR.strings.all_goBack),
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (searchResults.loadState.refresh is LoadState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        AnimatedVisibility(visible = query.isNotEmpty()) {
                            HapticIconButton(onClick = onClearSearchBar) {
                                Icon(
                                    Icons.Filled.Cancel,
                                    contentDescription = stringResource(MR.strings.search_clear_cd),
                                )
                            }
                        }
                    }
                },
            )
        },
        expanded = isSearchExpanded,
        onExpandedChange = onSearchExpandedChange,
        modifier = modifier.padding(16.dp),
        content = {
            SearchResultsList(
                onItemClick = { stream ->
                    onChannelClick(stream.user.id)
                },
                searchResults = searchResults,
            )
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FullHeightSearchBar(
    modifier: Modifier,
    query: String,
    isSearchExpanded: Boolean,
    searchResults: LazyPagingItems<ChannelSearchResult>,
    onChannelClick: (userId: String) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchExpandedChange: (Boolean) -> Unit,
    onClearSearchBar: () -> Unit,
    onDismissSearchBar: () -> Unit,
) {
    val padding by animateDpAsState(
        targetValue = if (isSearchExpanded) 0.dp else 16.dp,
        label = "inner padding",
    )

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = isSearchExpanded,
                onExpandedChange = onSearchExpandedChange,
                placeholder = { Text(stringResource(MR.strings.search_hint)) },
                leadingIcon = {
                    Crossfade(
                        targetState = isSearchExpanded,
                        label = "search leading icon",
                    ) { isActive ->
                        HapticIconButton(
                            onClick = onDismissSearchBar,
                            enabled = isActive,
                        ) {
                            if (isActive) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(MR.strings.all_goBack),
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                },
                trailingIcon = {
                    AnimatedVisibility(visible = query.isNotEmpty()) {
                        HapticIconButton(onClick = onClearSearchBar) {
                            Icon(
                                Icons.Filled.Cancel,
                                contentDescription = stringResource(MR.strings.search_clear_cd),
                            )
                        }
                    }
                },
            )
        },
        expanded = isSearchExpanded,
        onExpandedChange = onSearchExpandedChange,
        modifier = modifier.padding(padding),
        content = {
            SearchResultsList(
                onItemClick = { stream ->
                    onChannelClick(stream.user.id)
                },
                searchResults = searchResults,
            )
        },
    )
}
