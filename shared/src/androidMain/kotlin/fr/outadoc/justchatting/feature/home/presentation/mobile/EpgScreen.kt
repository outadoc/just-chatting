package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import app.cash.paging.compose.collectAsLazyPagingItems
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.presentation.EpgViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun EpgScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: EpgViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    when (val currentState = state) {
        is EpgViewModel.State.Loading -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }

        is EpgViewModel.State.Loaded -> {
            EpgContent(
                modifier = modifier,
                pagingData = currentState.pagingData,
            )
        }
    }
}

@Composable
private fun EpgContent(
    modifier: Modifier = Modifier,
    pagingData: Flow<PagingData<ChannelSchedule>>,
) {
    val items = pagingData.collectAsLazyPagingItems()
    LazyColumn(modifier = modifier) {
        items(
            count = items.itemCount,
            key = { index -> items[index]?.user?.id ?: index },
        ) { index ->
            val item: ChannelSchedule? = items[index]

            if (item == null) {
                CircularProgressIndicator()
            } else {
                EpgRow(
                    modifier = Modifier.height(100.dp),
                    channelSchedule = item,
                )
            }

            if (index < items.itemCount - 1) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun EpgRow(
    modifier: Modifier = Modifier,
    channelSchedule: ChannelSchedule,
) {
    val segments = channelSchedule.segments.collectAsLazyPagingItems()

    LazyRow(modifier = modifier) {
        item(key = channelSchedule.user.id) {
            Text(channelSchedule.user.displayName)
        }

        items(
            count = segments.itemCount,
            key = { index -> segments[index]?.id ?: index },
        ) { index ->
            val item = segments[index]

            if (item == null) {
                CircularProgressIndicator()
            } else {
                EpgSegment(
                    segment = item,
                )
            }
        }
    }
}

@Composable
private fun EpgSegment(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
) {
    Column(modifier = modifier) {
        Text("${segment.startTime} ‑ ${segment.endTime}")
        Text(segment.title)
    }
}
