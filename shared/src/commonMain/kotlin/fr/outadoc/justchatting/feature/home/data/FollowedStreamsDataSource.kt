package fr.outadoc.justchatting.feature.home.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.feature.home.domain.model.Pagination
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.datetime.Instant

internal class FollowedStreamsDataSource(
    private val userId: String?,
    private val twitchClient: TwitchClient,
) : PagingSource<Pagination, List<Stream>>() {

    override fun getRefreshKey(state: PagingState<Pagination, List<Stream>>): Pagination? = null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, List<Stream>> {
        return twitchClient
            .getFollowedStreams(
                userId = userId,
                limit = params.loadSize,
                after = (params.key as? Pagination.Next)?.cursor,
            )
            .fold(
                onSuccess = { response ->
                    val itemsAfter: Int =
                        if (response.pagination.cursor == null) {
                            0
                        } else {
                            LoadResult.Page.COUNT_UNDEFINED
                        }

                    LoadResult.Page(
                        data = listOf(
                            response.data.map { stream ->
                                Stream(
                                    id = stream.id,
                                    userId = stream.userId,
                                    category = if (stream.gameId != null && stream.gameName != null) {
                                        StreamCategory(
                                            id = stream.gameId,
                                            name = stream.gameName,
                                        )
                                    } else {
                                        null
                                    },
                                    title = stream.title,
                                    viewerCount = stream.viewerCount,
                                    startedAt = Instant.parse(stream.startedAt),
                                    tags = stream.tags.toPersistentSet(),
                                )
                            },
                        ),
                        prevKey = null,
                        nextKey = response.pagination.cursor?.let { cursor -> Pagination.Next(cursor) },
                        itemsAfter = itemsAfter,
                    )
                },
                onFailure = { exception ->
                    logError<FollowedStreamsDataSource>(exception) { "Error while fetching followed streams" }
                    LoadResult.Error(exception)
                },
            )
    }
}
