package fr.outadoc.justchatting.feature.home.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.feature.home.domain.model.Pagination
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.collections.immutable.toPersistentList

internal class FollowedStreamsDataSource(
    private val userId: String?,
    private val twitchClient: TwitchClient,
) : PagingSource<Pagination, Stream>() {

    override fun getRefreshKey(state: PagingState<Pagination, Stream>): Pagination? = null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, Stream> {
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
                        data = response.data.map { stream ->
                            Stream(
                                id = stream.id,
                                user = User(
                                    id = stream.userId,
                                    login = stream.userLogin,
                                    displayName = stream.userName,
                                ),
                                gameName = stream.gameName,
                                title = stream.title,
                                viewerCount = stream.viewerCount,
                                startedAt = stream.startedAt,
                                tags = stream.tags.toPersistentList(),
                            )
                        },
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
