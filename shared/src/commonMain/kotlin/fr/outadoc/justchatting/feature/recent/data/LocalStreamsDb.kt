package fr.outadoc.justchatting.feature.recent.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.outadoc.justchatting.data.db.StreamQueries
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.recent.domain.LocalStreamsApi
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class LocalStreamsDb(
    private val streamQueries: StreamQueries,
    private val clock: Clock,
) : LocalStreamsApi {

    override fun getPastStreams(
        userId: String,
        notBefore: Instant,
        notAfter: Instant
    ): Flow<List<ChannelScheduleSegment>> {
        return streamQueries
            .getPastStreams(
                userId = userId,
                notBefore = notBefore.toEpochMilliseconds(),
                notAfter = notAfter.toEpochMilliseconds()
            )
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { streams ->
                streams.map { stream ->
                    ChannelScheduleSegment(
                        id = stream.id,
                        startTime = Instant.fromEpochMilliseconds(stream.start_time),
                        endTime = Instant.fromEpochMilliseconds(stream.end_time),
                        title = stream.title,
                        category = if (stream.category_id != null && stream.category_name != null) {
                            StreamCategory(
                                id = stream.category_id,
                                name = stream.category_name
                            )
                        } else {
                            null
                        },
                    )
                }
            }
    }

    override fun getLiveStreams(
        userId: String,
        notBefore: Instant,
        notAfter: Instant
    ): Flow<List<Stream>> {
        return streamQueries
            .getLiveStreams(userId = userId)
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
                                name = stream.category_name
                            )
                        } else {
                            null
                        },
                        tags = stream.tags.split(',').toPersistentSet()
                    )
                }
            }
    }

    override fun getFutureStreams(
        userId: String,
        notBefore: Instant,
        notAfter: Instant
    ): Flow<List<ChannelScheduleSegment>> {
        return streamQueries
            .getFutureStreams(
                userId = userId,
                notBefore = notBefore.toEpochMilliseconds(),
                notAfter = notAfter.toEpochMilliseconds()
            )
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { streams ->
                streams.map { stream ->
                    ChannelScheduleSegment(
                        id = stream.id,
                        startTime = Instant.fromEpochMilliseconds(stream.start_time),
                        endTime = Instant.fromEpochMilliseconds(stream.end_time),
                        title = stream.title,
                        category = if (stream.category_id != null && stream.category_name != null) {
                            StreamCategory(
                                id = stream.category_id,
                                name = stream.category_name
                            )
                        } else {
                            null
                        },
                    )
                }
            }
    }
}
