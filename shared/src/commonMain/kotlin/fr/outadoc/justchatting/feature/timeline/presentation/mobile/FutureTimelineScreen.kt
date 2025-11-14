package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.mobile.Screen
import fr.outadoc.justchatting.feature.timeline.presentation.FutureTimelineViewModel
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.timeline_future
import fr.outadoc.justchatting.shared.timeline_refresh_action_cd
import fr.outadoc.justchatting.shared.timeline_today_action_cd
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FutureTimelineScreen(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
) {
    val viewModel: FutureTimelineViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.syncEverythingNow()
    }

    val listState = rememberLazyListState()

    MainNavigation(
        selectedScreen = Screen.Future,
        onSelectedTabChange = onNavigate,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            ) {
                TopAppBar(
                    title = { Text(stringResource(Res.string.timeline_future)) },
                    actions = {
                        AccessibleIconButton(
                            onClickLabel = stringResource(Res.string.timeline_today_action_cd),
                            onClick = {
                                coroutineScope.launch {
                                    listState.scrollToItem(index = 0)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                            )
                        }

                        AccessibleIconButton(
                            onClickLabel = stringResource(Res.string.timeline_refresh_action_cd),
                            onClick = { viewModel.syncEverythingNow() },
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                )
                            }
                        }
                    },
                )
            }
        },
        content = { insets ->
            FutureTimelineContent(
                modifier = modifier,
                insets = insets,
                future = state.future,
                listState = listState,
            )
        },
    )
}
