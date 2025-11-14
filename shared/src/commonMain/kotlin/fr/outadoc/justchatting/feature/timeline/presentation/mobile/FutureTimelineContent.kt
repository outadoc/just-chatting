package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.mobile.BasicUserInfo
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ExtraUserInfo
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.mobile.NoContent
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.utils.presentation.formatDate
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FutureTimelineContent(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    future: ImmutableMap<LocalDate, List<ChannelScheduleSegment>>,
    listState: LazyListState,
) {
    var showUserDetails: User? by remember { mutableStateOf(null) }

    if (future.isEmpty()) {
        NoContent(
            modifier = modifier
                .padding(insets)
                .fillMaxSize(),
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(insets)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            future.keys.forEach { date ->
                stickyHeader(
                    key = "header-${date.toEpochDays()}",
                    contentType = "header",
                ) {
                    SectionHeader(
                        title = { Text(date.formatDate(isFuture = true)) },
                    )
                }

                items(
                    items = future[date].orEmpty(),
                    key = { segment -> segment.id },
                    contentType = { "segment" },
                ) { segment ->
                    FutureTimelineSegment(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth(),
                        segment = segment,
                        onUserClick = {
                            showUserDetails = segment.user
                        },
                    )
                }
            }
        }
    }

    showUserDetails?.let { user ->
        ActionBottomSheet(
            onDismissRequest = { showUserDetails = null },
            header = {
                BasicUserInfo(user = user)
            },
            content = {
                ExtraUserInfo(user = user)
            },
        )
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    0f to MaterialTheme.colorScheme.surface,
                    1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                ),
            )
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.titleMedium,
        ) {
            title()
        }
    }
}
