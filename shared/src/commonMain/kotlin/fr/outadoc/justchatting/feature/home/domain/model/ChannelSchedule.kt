package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

@Stable
internal data class ChannelSchedule(
    val scheduleFlow: Flow<PagingData<ChannelScheduleForDay>>,
    val user: User,
)
