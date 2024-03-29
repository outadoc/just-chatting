package fr.outadoc.justchatting.component.chatapi.domain.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.component.chatapi.domain.model.Pagination
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.twitch.http.api.HelixApi
import fr.outadoc.justchatting.component.twitch.http.model.StreamsResponse
import fr.outadoc.justchatting.utils.logging.logError

class FollowedStreamsDataSource(
    private val userId: String?,
    private val helixApi: HelixApi,
) : PagingSource<Pagination, List<Stream>>() {

    override fun getRefreshKey(state: PagingState<Pagination, List<Stream>>): Pagination? = null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, List<Stream>> {
        return try {
            val response: StreamsResponse =
                helixApi.getFollowedStreams(
                    userId = userId,
                    limit = params.loadSize,
                    after = (params.key as? Pagination.Next)?.cursor,
                )

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
                            userLogin = stream.userLogin,
                            userName = stream.userName,
                            gameName = stream.gameName,
                            title = stream.title,
                            viewerCount = stream.viewerCount,
                            startedAt = stream.startedAt,
                            tags = stream.tags,
                        )
                    },
                ),
                prevKey = null,
                nextKey = response.pagination.cursor?.let { cursor -> Pagination.Next(cursor) },
                itemsAfter = itemsAfter,
            )
        } catch (e: Exception) {
            logError<FollowedStreamsDataSource>(e) { "Error while fetching followed streams" }
            LoadResult.Error(e)
        }
    }
}
