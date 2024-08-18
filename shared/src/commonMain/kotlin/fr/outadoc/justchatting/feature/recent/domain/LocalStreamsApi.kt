package fr.outadoc.justchatting.feature.recent.domain

import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal interface LocalStreamsApi {

    fun getPastStreams(
        notBefore: Instant,
        notAfter: Instant
    ): Flow<List<ChannelScheduleSegment>>

    fun getLiveStreams(): Flow<List<Stream>>

    fun getFutureStreams(
        notBefore: Instant,
        notAfter: Instant
    ): Flow<List<ChannelScheduleSegment>>
}
