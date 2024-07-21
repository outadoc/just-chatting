package fr.outadoc.justchatting.feature.home.domain.model

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

internal data class ChannelSchedule(
    val scheduleFlow: Flow<PagingData<ChannelScheduleForDay>>,
    val user: User,
)
