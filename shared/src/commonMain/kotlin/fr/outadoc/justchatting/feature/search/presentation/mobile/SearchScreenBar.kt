package fr.outadoc.justchatting.feature.search.presentation.mobile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.window.core.layout.WindowHeightSizeClass
import app.cash.paging.compose.LazyPagingItems
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.all_goBack
import fr.outadoc.justchatting.shared.search_clear_cd
import fr.outadoc.justchatting.shared.search_hint
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchScreenBar(
    modifier: Modifier = Modifier,
    searchResults: LazyPagingItems<ChannelSearchResult>,
    query: String,
    isSearchExpanded: Boolean,
    onChannelClick: (userId: String) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onClearSearchBar: () -> Unit,
    onDismissSearchBar: () -> Unit,
) {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    when (sizeClass.windowHeightSizeClass) {
        WindowHeightSizeClass.COMPACT,
        WindowHeightSizeClass.MEDIUM,
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

        WindowHeightSizeClass.EXPANDED -> {
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
                placeholder = {
                    Text(
                        stringResource(Res.string.search_hint),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                leadingIcon = {
                    AnimatedContent(
                        targetState = isSearchExpanded,
                        label = "search leading icon",
                    ) { isActive ->
                        if (isActive) {
                            AccessibleIconButton(
                                onClick = onDismissSearchBar,
                                onClickLabel = stringResource(Res.string.all_goBack),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                            )
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
                            AccessibleIconButton(
                                onClick = onClearSearchBar,
                                onClickLabel = stringResource(Res.string.search_clear_cd),
                            ) {
                                Icon(
                                    Icons.Filled.Cancel,
                                    contentDescription = null,
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
                placeholder = {
                    Text(
                        stringResource(Res.string.search_hint),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                leadingIcon = {
                    AnimatedContent(
                        targetState = isSearchExpanded,
                        label = "search leading icon",
                    ) { isActive ->
                        if (isActive) {
                            AccessibleIconButton(
                                onClick = onDismissSearchBar,
                                onClickLabel = stringResource(Res.string.all_goBack),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                            )
                        }
                    }
                },
                trailingIcon = {
                    AnimatedVisibility(visible = query.isNotEmpty()) {
                        AccessibleIconButton(
                            onClick = onClearSearchBar,
                            onClickLabel = stringResource(Res.string.search_clear_cd),
                        ) {
                            Icon(
                                Icons.Filled.Cancel,
                                contentDescription = null,
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
