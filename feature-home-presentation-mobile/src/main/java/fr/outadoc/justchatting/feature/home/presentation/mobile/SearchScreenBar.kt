package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.home.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.utils.ui.HapticIconButton
import org.koin.androidx.compose.getViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SearchScreenBar(
    modifier: Modifier = Modifier,
    onChannelClick: (login: String) -> Unit,
) {
    val viewModel = getViewModel<ChannelSearchViewModel>()
    val state by viewModel.state.collectAsState()

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
        leadingIcon = (
            @Composable {
                AnimatedVisibility(visible = state.isActive) {
                    HapticIconButton(onClick = viewModel::onDismiss) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.all_goBack),
                        )
                    }
                }
            }
            ).takeIf { state.isActive },
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
