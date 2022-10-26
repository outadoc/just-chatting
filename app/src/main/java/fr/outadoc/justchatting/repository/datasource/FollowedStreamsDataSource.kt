package fr.outadoc.justchatting.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.model.helix.stream.StreamsResponse

class FollowedStreamsDataSource(
    private val userId: String?,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixApi: HelixApi
) : PagingSource<String, StreamsResponse>() {

    override fun getRefreshKey(state: PagingState<String, StreamsResponse>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.pagination?.cursor
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, StreamsResponse> {
        try {
            if (helixToken.isNullOrBlank()) error("Helix token is null")

            val response = helixApi.getFollowedStreams(
                clientId = helixClientId,
                token = helixToken,
                userId = userId,
                limit = params.loadSize,
                offset = params.key
            )

            return LoadResult.Page(
                data = listOf(response),
                nextKey = response.pagination?.cursor,
                prevKey = null
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}
