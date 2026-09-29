package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainTopAppBar
import fr.outadoc.justchatting.feature.shared.presentation.ui.ProfileButton
import fr.outadoc.justchatting.feature.timeline.presentation.FutureTimelineViewModel
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.timeline_future
import fr.outadoc.justchatting.shared.internal.timeline_refresh_action_cd
import fr.outadoc.justchatting.shared.internal.timeline_today_action_cd
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import fr.outadoc.justchatting.utils.presentation.rememberHasPointingDevice
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FutureTimelineScreen(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: FutureTimelineViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val hasMouse = rememberHasPointingDevice()

    LaunchedEffect(Unit) {
        viewModel.syncEverythingNow()
    }

    val listState = rememberLazyListState()

    var selectedDate: LocalDate? by remember { mutableStateOf(null) }

    MainNavigation(
        selectedScreen = Screen.Future,
        onSelectedTabChange = onNavigate,
        topBar = {
            MainTopAppBar(
                title = stringResource(Res.string.timeline_future),
                actions = {
                    AccessibleIconButton(
                        onClickLabel = stringResource(Res.string.timeline_today_action_cd),
                        onClick = {
                            selectedDate = null
                            coroutineScope.launch {
                                listState.scrollToItem(index = 0)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                        )
                    }

                    if (hasMouse) {
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
                    }
                },
                profileButton = {
                    ProfileButton(onClick = onOpenSettings)
                },
            )
        },
        content = { insets ->
            FutureTimelineContent(
                modifier = modifier,
                insets = insets,
                days = state.days,
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.syncEverythingNow() },
                showRefreshIndicator = !hasMouse,
                listState = listState,
                selectedDate = selectedDate,
                onSelectedDateChange = { date ->
                    selectedDate = date
                    coroutineScope.launch {
                        listState.scrollToItem(index = 0)
                    }
                },
            )
        },
    )
}
