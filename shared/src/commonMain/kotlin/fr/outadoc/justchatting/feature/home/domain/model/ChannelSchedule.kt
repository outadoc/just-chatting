package fr.outadoc.justchatting.feature.home.domain.model

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

internal data class ChannelSchedule(
    val segments: Flow<PagingData<ChannelScheduleSegment>>,
    val user: User,
)
