package fr.outadoc.justchatting.feature.home.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.outadoc.justchatting.data.db.StreamQueries
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.home.domain.model.Video
import fr.outadoc.justchatting.feature.recent.domain.LocalStreamsApi
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logDebug
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

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
            .flowOn(DispatchersProvider.io)
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
            .flowOn(DispatchersProvider.io)
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
            .flowOn(DispatchersProvider.io)
    }

    override suspend fun savePastStreams(
        user: User,
        videos: List<Video>,
    ) {
        withContext(DispatchersProvider.io) {
            val now = clock.now()
            streamQueries.transaction {
                videos.forEach { video ->
                    streamQueries.addPastStream(
                        id = video.id,
                        user_id = video.userId,
                        // TODO check that this is correct
                        start_time = (video.createdAt - video.duration).toEpochMilliseconds(),
                        end_time = video.createdAt.toEpochMilliseconds(),
                        title = video.title,
                        category_id = null,
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
        withContext(DispatchersProvider.io) {
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
                        end_time = segment.endTime.toEpochMilliseconds(),
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
            )
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .flowOn(DispatchersProvider.io)
    }

    override suspend fun cleanup(
        notBefore: Instant,
        notAfter: Instant,
    ) = withContext(DispatchersProvider.io) {
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
