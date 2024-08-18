package fr.outadoc.justchatting.feature.recent.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.outadoc.justchatting.data.db.StreamQueries
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.home.domain.model.Video
import fr.outadoc.justchatting.feature.recent.domain.LocalStreamsApi
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class LocalStreamsDb(
    private val streamQueries: StreamQueries,
    private val clock: Clock,
) : LocalStreamsApi {

    override fun getPastStreams(
        notBefore: Instant,
        notAfter: Instant,
    ): Flow<List<ChannelScheduleSegment>> {
        return streamQueries
            .getPastStreams(
                notBefore = notBefore.toEpochMilliseconds(),
                notAfter = notAfter.toEpochMilliseconds(),
            )
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { streams ->
                streams.map { stream ->
                    ChannelScheduleSegment(
                        id = stream.id,
                        userId = stream.user_id,
                        startTime = Instant.fromEpochMilliseconds(stream.start_time),
                        endTime = Instant.fromEpochMilliseconds(stream.end_time),
                        title = stream.title,
                        category = if (stream.category_id != null && stream.category_name != null) {
                            StreamCategory(
                                id = stream.category_id,
                                name = stream.category_name,
                            )
                        } else {
                            null
                        },
                    )
                }
            }
    }

    override fun getLiveStreams(): Flow<List<Stream>> {
        return streamQueries
            .getLiveStreams()
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { streams ->
                streams.map { stream ->
                    Stream(
                        id = stream.id,
                        userId = stream.user_id,
                        startedAt = Instant.fromEpochMilliseconds(stream.start_time),
                        title = stream.title,
                        viewerCount = stream.viewer_count,
                        category = if (stream.category_id != null && stream.category_name != null) {
                            StreamCategory(
                                id = stream.category_id,
                                name = stream.category_name,
                            )
                        } else {
                            null
                        },
                        tags = stream.tags.split(',').toPersistentSet(),
                    )
                }
            }
    }

    override fun getFutureStreams(
        notBefore: Instant,
        notAfter: Instant,
    ): Flow<List<ChannelScheduleSegment>> {
        return streamQueries
            .getFutureStreams(
                notBefore = notBefore.toEpochMilliseconds(),
                notAfter = notAfter.toEpochMilliseconds(),
            )
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { streams ->
                streams.map { stream ->
                    ChannelScheduleSegment(
                        id = stream.id,
                        userId = stream.user_id,
                        startTime = Instant.fromEpochMilliseconds(stream.start_time),
                        endTime = Instant.fromEpochMilliseconds(stream.end_time),
                        title = stream.title,
                        category = if (stream.category_id != null && stream.category_name != null) {
                            StreamCategory(
                                id = stream.category_id,
                                name = stream.category_name,
                            )
                        } else {
                            null
                        },
                    )
                }
            }
    }

    override suspend fun addPastStreams(videos: List<Video>) {
        TODO("not implemented")
    }

    override suspend fun addFutureStreams(segments: List<ChannelScheduleSegment>) {
        withContext(DispatchersProvider.io) {
            streamQueries.transaction {
                segments.forEach { segment ->
                    segment.category?.let { category ->
                        streamQueries.addCategory(
                            id = category.id,
                            name = category.name,
                        )
                    }

                    streamQueries.addFutureStream(
                        id = segment.id,
                        user_id = segment.userId,
                        start_time = segment.startTime.toEpochMilliseconds(),
                        end_time = segment.endTime.toEpochMilliseconds(),
                        title = segment.title,
                        category_id = segment.category?.id,
                    )
                }
            }
        }
    }

    override suspend fun cleanup(
        notBefore: Instant,
        notAfter: Instant,
    ) = withContext(DispatchersProvider.io) {
        val now = clock.now()
        streamQueries.transaction {
            streamQueries.cleanupAllLiveStreams()
            streamQueries.cleanupPastStreams(
                notBefore = notBefore.toEpochMilliseconds(),
            )
            streamQueries.cleanupFutureStreams(
                notBefore = notBefore.toEpochMilliseconds(),
                notAfter = notAfter.toEpochMilliseconds(),
                now = now.toEpochMilliseconds(),
            )
            streamQueries.cleanupCategories()
        }
    }
}
