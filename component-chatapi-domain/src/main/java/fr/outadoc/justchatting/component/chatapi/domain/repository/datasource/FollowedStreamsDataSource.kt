package fr.outadoc.justchatting.component.chatapi.domain.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.component.chatapi.domain.model.Pagination
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.StreamsResponse
import fr.outadoc.justchatting.component.twitch.api.HelixApi

class FollowedStreamsDataSource(
    private val userId: String?,
    private val helixApi: HelixApi
) : PagingSource<String, StreamsResponse>() {

    override fun getRefreshKey(state: PagingState<String, StreamsResponse>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.pagination?.cursor
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, StreamsResponse> {
        return try {
            val response = helixApi.getFollowedStreams(
                userId = userId,
                limit = params.loadSize,
                offset = params.key
            )

            LoadResult.Page(
                data = listOf(
                    StreamsResponse(
                        data = response.data?.map { stream ->
                            Stream(
                                id = stream.id,
                                userId = stream.userId,
                                userLogin = stream.userLogin,
                                userName = stream.userName,
                                gameId = stream.gameId,
                                gameName = stream.gameName,
                                type = stream.type,
                                title = stream.title,
                                viewerCount = stream.viewerCount,
                                startedAt = stream.startedAt
                            )
                        },
                        pagination = Pagination(
                            cursor = response.pagination?.cursor
                        )
                    )
                ),
                nextKey = response.pagination?.cursor,
                prevKey = null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
