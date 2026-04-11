package fr.outadoc.justchatting.feature.shared.domain

import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.Video
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

public interface LocalStreamsApi {
    public fun getPastStreams(
        notBefore: Instant,
        notAfter: Instant,
    ): Flow<List<ChannelScheduleSegment>>

    public fun getMostRecentPastStream(user: User): Flow<Instant?>

    public fun getLiveStreams(): Flow<List<Stream>>

    public fun getFutureStreams(
        notBefore: Instant,
        notAfter: Instant,
    ): Flow<List<ChannelScheduleSegment>>

    public suspend fun cleanup(
        notBefore: Instant,
        notAfter: Instant,
    )

    public suspend fun savePastStreams(
        user: User,
        videos: List<Video>,
    )

    public suspend fun saveAndReplaceLiveStreams(streams: List<Stream>)

    public suspend fun saveFutureStreams(
        user: User,
        segments: List<ChannelScheduleSegment>,
    )

    public fun getUserIdsToSync(): Flow<List<String>>
}
