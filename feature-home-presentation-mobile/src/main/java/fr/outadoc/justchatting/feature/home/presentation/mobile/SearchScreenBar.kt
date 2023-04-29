package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.home.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.utils.ui.HapticIconButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreenBar(
    modifier: Modifier = Modifier,
    onChannelClick: (login: String) -> Unit,
    sizeClass: WindowSizeClass,
) {
    val viewModel = koinViewModel<ChannelSearchViewModel>()
    val state by viewModel.state.collectAsState()

    when (sizeClass.heightSizeClass) {
        WindowHeightSizeClass.Compact,
        WindowHeightSizeClass.Medium,
        -> {
            FullHeightSearchBar(
                state = state,
                modifier = modifier,
                viewModel = viewModel,
                onChannelClick = onChannelClick,
            )
        }

        WindowHeightSizeClass.Expanded -> {
            CompactSearchBar(
                modifier = modifier,
                state = state,
                viewModel = viewModel,
                onChannelClick = onChannelClick,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CompactSearchBar(
    modifier: Modifier,
    state: ChannelSearchViewModel.State,
    viewModel: ChannelSearchViewModel,
    onChannelClick: (login: String) -> Unit,
) {
    DockedSearchBar(
        modifier = modifier.padding(16.dp),
        query = state.query,
        onQueryChange = viewModel::onQueryChange,
        onSearch = {},
        active = state.isActive,
        onActiveChange = viewModel::onActiveChange,
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = {
            Crossfade(
                targetState = state.isActive,
                label = "search leading icon",
            ) { isActive ->
                HapticIconButton(
                    onClick = viewModel::onDismiss,
                    enabled = isActive,
                ) {
                    if (isActive) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.all_goBack),
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
            AnimatedVisibility(visible = state.query.isNotEmpty()) {
                HapticIconButton(onClick = viewModel::onClear) {
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = stringResource(R.string.search_clear_cd),
                    )
                }
            }
        },
        content = {
            SearchResultsList(
                onItemClick = { stream ->
                    stream.broadcasterLogin?.let { login ->
                        onChannelClick(login)
                    }
                },
                viewModel = viewModel,
            )
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FullHeightSearchBar(
    state: ChannelSearchViewModel.State,
    modifier: Modifier,
    viewModel: ChannelSearchViewModel,
    onChannelClick: (login: String) -> Unit,
) {
    val padding by animateDpAsState(
        targetValue = if (state.isActive) 0.dp else 16.dp,
        label = "inner padding",
    )

    SearchBar(
        modifier = modifier.padding(padding),
        query = state.query,
        onQueryChange = viewModel::onQueryChange,
        onSearch = {},
        active = state.isActive,
        onActiveChange = viewModel::onActiveChange,
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = {
            Crossfade(
                targetState = state.isActive,
                label = "search leading icon",
            ) { isActive ->
                HapticIconButton(
                    onClick = viewModel::onDismiss,
                    enabled = isActive,
                ) {
                    if (isActive) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.all_goBack),
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
            AnimatedVisibility(visible = state.query.isNotEmpty()) {
                HapticIconButton(onClick = viewModel::onClear) {
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = stringResource(R.string.search_clear_cd),
                    )
                }
            }
        },
        content = {
            SearchResultsList(
                onItemClick = { stream ->
                    stream.broadcasterLogin?.let { login ->
                        onChannelClick(login)
                    }
                },
                viewModel = viewModel,
            )
        },
    )
}
