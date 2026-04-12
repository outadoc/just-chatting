package fr.outadoc.justchatting.feature.shared.domain

import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.Video
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

internal interface LocalStreamsApi {
    fun getPastStreams(
        notBefore: Instant,
        notAfter: Instant,
    ): Flow<List<ChannelScheduleSegment>>

    fun getMostRecentPastStream(user: User): Flow<Instant?>

    fun getLiveStreams(): Flow<List<Stream>>

    fun getFutureStreams(
        notBefore: Instant,
        notAfter: Instant,
    ): Flow<List<ChannelScheduleSegment>>

    suspend fun cleanup(
        notBefore: Instant,
        notAfter: Instant,
    )

    suspend fun savePastStreams(
        user: User,
        videos: List<Video>,
    )

    suspend fun saveAndReplaceLiveStreams(streams: List<Stream>)

    suspend fun saveFutureStreams(
        user: User,
        segments: List<ChannelScheduleSegment>,
    )

    fun getUserIdsToSync(): Flow<List<String>>
}
