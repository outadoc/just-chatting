package fr.outadoc.justchatting.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.ui.search.channels.ChannelSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    modifier: Modifier = Modifier,
    selectedTab: Tab,
    searchViewModel: ChannelSearchViewModel
) {
    val searchState by searchViewModel.state.collectAsState()
    Crossfade(targetState = selectedTab) {
        when (selectedTab) {
            Tab.Search -> {
                SearchTopAppBar(
                    modifier = modifier,
                    query = searchState.query,
                    onQueryChange = { newQuery ->
                        searchViewModel.onQueryChange(newQuery)
                    }
                )
            }

            else -> {
                TopAppBar(
                    modifier = modifier,
                    title = { Text(stringResource(R.string.app_name)) }
                )
            }
        }
    }
}
