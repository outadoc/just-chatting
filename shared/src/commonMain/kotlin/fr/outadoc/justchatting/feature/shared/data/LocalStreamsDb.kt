package fr.outadoc.justchatting.feature.shared.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import fr.outadoc.justchatting.data.db.StreamQueries
import fr.outadoc.justchatting.feature.shared.domain.LocalStreamsApi
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.domain.model.Video
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logDebug
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

internal class LocalStreamsDb(
    private val streamQueries: StreamQueries,
    private val clock: Clock,
    private val dispatchersProvider: DispatchersProvider,
) : LocalStreamsApi {
    override fun getPastStreams(
        notBefore: Instant,
        notAfter: Instant,
    ): Flow<List<ChannelScheduleSegment>> = streamQueries
        .getPastStreams(
            notBefore = notBefore.toEpochMilliseconds(),
            notAfter = notAfter.toEpochMilliseconds(),
        ).asFlow()
        .mapToList(dispatchersProvider.io)
        .map { streams ->
            streams.map { stream ->
                val categoryId = stream.category_id
                val categoryName = stream.category_name
                ChannelScheduleSegment(
                    id = stream.id,
                    user = User(
                        id = stream.user_id,
                        login = stream.login,
                        displayName = stream.display_name,
                        profileImageUrl = stream.profile_image_url,
                        description = stream.description,
                        createdAt = Instant.fromEpochMilliseconds(stream.created_at),
                        usedAt = if (stream.used_at > 0) {
                            Instant.fromEpochMilliseconds(stream.used_at)
                        } else {
                            null
                        },
                    ),
                    startTime = Instant.fromEpochMilliseconds(stream.start_time),
                    endTime = stream.end_time
                        .takeIf { it > 0 }
                        ?.let { Instant.fromEpochMilliseconds(it) },
                    title = stream.title,
                    category = if (categoryId != null && categoryName != null) {
                        StreamCategory(
                            id = categoryId,
                            name = categoryName,
                        )
                    } else {
                        null
                    },
                )
            }
        }.flowOn(dispatchersProvider.io)

    override fun getMostRecentPastStream(user: User): Flow<Instant?> = streamQueries
        .getMostRecentPastStream(user.id)
        .asFlow()
        .mapToOneOrNull(dispatchersProvider.io)
        .map { endTime: Long? ->
            endTime?.let { Instant.fromEpochMilliseconds(it) }
        }.flowOn(dispatchersProvider.io)

    override fun getLiveStreams(): Flow<List<Stream>> = streamQueries
        .getLiveStreams()
        .asFlow()
        .mapToList(dispatchersProvider.io)
        .map { streams ->
            streams.map { stream ->
                val categoryId = stream.category_id
                val categoryName = stream.category_name
                Stream(
                    id = stream.id,
                    userId = stream.user_id,
                    startedAt = Instant.fromEpochMilliseconds(stream.start_time),
                    title = stream.title,
                    viewerCount = stream.viewer_count,
                    category = if (categoryId != null && categoryName != null) {
                        StreamCategory(
                            id = categoryId,
                            name = categoryName,
                        )
                    } else {
                        null
                    },
                    tags = stream.tags.split(',').toPersistentSet(),
                )
            }
        }.flowOn(dispatchersProvider.io)

    override fun getFutureStreams(
        notBefore: Instant,
        notAfter: Instant,
    ): Flow<List<ChannelScheduleSegment>> = streamQueries
        .getFutureStreams(
            notBefore = notBefore.toEpochMilliseconds(),
            notAfter = notAfter.toEpochMilliseconds(),
        ).asFlow()
        .mapToList(dispatchersProvider.io)
        .map { streams ->
            streams.map { stream ->
                val categoryId = stream.category_id
                val categoryName = stream.category_name
                ChannelScheduleSegment(
                    id = stream.id,
                    user = User(
                        id = stream.user_id,
                        login = stream.login,
                        displayName = stream.display_name,
                        profileImageUrl = stream.profile_image_url,
                        description = stream.description,
                        createdAt = Instant.fromEpochMilliseconds(stream.created_at),
                        usedAt = if (stream.used_at > 0) {
                            Instant.fromEpochMilliseconds(stream.used_at)
                        } else {
                            null
                        },
                    ),
                    startTime = Instant.fromEpochMilliseconds(stream.start_time),
                    endTime = stream.end_time
                        .takeIf { it > 0 }
                        ?.let { Instant.fromEpochMilliseconds(it) },
                    title = stream.title,
                    category = if (categoryId != null && categoryName != null) {
                        StreamCategory(
                            id = categoryId,
                            name = categoryName,
                        )
                    } else {
                        null
                    },
                )
            }
        }.flowOn(dispatchersProvider.io)

    override suspend fun savePastStreams(
        user: User,
        videos: List<Video>,
    ) {
        withContext(dispatchersProvider.io) {
            val now = clock.now()
            streamQueries.transaction {
                videos.forEach { video ->
                    streamQueries.addPastStream(
                        id = video.id,
                        user_id = video.userId,
                        start_time = video.createdAt.toEpochMilliseconds(),
                        end_time = (video.createdAt + video.duration).toEpochMilliseconds(),
                        title = video.title,
                        category_id = null,
                        stream_id = video.streamId,
                    )
                }

                streamQueries.rememberUserUpdated(
                    user_id = user.id,
                    last_updated = now.toEpochMilliseconds(),
                )
            }
        }
    }

    override suspend fun saveAndReplaceLiveStreams(streams: List<Stream>) {
        val now = clock.now()
        streamQueries.transaction {
            streamQueries.cleanupAllLiveStreams()
            streams.forEach { stream ->
                stream.category?.let { category ->
                    streamQueries.addCategory(
                        id = category.id,
                        name = category.name,
                        inserted_at = now.toEpochMilliseconds(),
                    )
                }

                streamQueries.addLiveStream(
                    id = stream.id,
                    user_id = stream.userId,
                    start_time = stream.startedAt.toEpochMilliseconds(),
                    title = stream.title,
                    viewer_count = stream.viewerCount,
                    category_id = stream.category?.id,
                    tags = stream.tags.joinToString(","),
                )
            }
        }
    }

    override suspend fun saveFutureStreams(
        user: User,
        segments: List<ChannelScheduleSegment>,
    ) {
        withContext(dispatchersProvider.io) {
            val now = clock.now()
            streamQueries.transaction {
                segments.forEach { segment ->
                    segment.category?.let { category ->
                        streamQueries.addCategory(
                            id = category.id,
                            name = category.name,
                            inserted_at = now.toEpochMilliseconds(),
                        )
                    }

                    streamQueries.addFutureStream(
                        id = segment.id,
                        user_id = segment.user.id,
                        start_time = segment.startTime.toEpochMilliseconds(),
                        end_time = segment.endTime?.toEpochMilliseconds() ?: 0,
                        title = segment.title,
                        category_id = segment.category?.id,
                    )

                    streamQueries.rememberUserUpdated(
                        user_id = user.id,
                        last_updated = now.toEpochMilliseconds(),
                    )
                }
            }
        }
    }

    override fun getUserIdsToSync(): Flow<List<String>> {
        val minAcceptableCacheDate = clock.now() - MaxStreamSyncCacheLife

        logDebug<LocalStreamsDb> { "Updating schedule for users not updated after $minAcceptableCacheDate" }

        return streamQueries
            .getUserIdsToUpdate(
                minUpdatedAtTimestamp = minAcceptableCacheDate.toEpochMilliseconds(),
            ).asFlow()
            .mapToList(dispatchersProvider.io)
            .flowOn(dispatchersProvider.io)
    }

    override suspend fun cleanup(
        notBefore: Instant,
        notAfter: Instant,
    ) = withContext(dispatchersProvider.io) {
        val now = clock.now()
        streamQueries.transaction {
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

    private companion object {
        val MaxStreamSyncCacheLife = 1.hours
    }
}
