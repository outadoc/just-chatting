package fr.outadoc.justchatting.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.component.twitch.model.FollowResponse

class FollowedChannelsDataSource(
    private val userId: String?,
    private val helixApi: HelixApi
) : PagingSource<String, FollowResponse>() {

    override fun getRefreshKey(state: PagingState<String, FollowResponse>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.pagination?.cursor
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, FollowResponse> {
        return try {
            val response = helixApi.getFollowedChannels(
                userId = userId,
                limit = params.loadSize,
                offset = params.key
            )

            LoadResult.Page(
                data = listOf(response),
                nextKey = response.pagination?.cursor,
                prevKey = null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
