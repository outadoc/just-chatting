package fr.outadoc.justchatting.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.component.twitch.model.ChannelSearchResponse

class SearchChannelsDataSource(
    private val query: String,
    private val helixApi: HelixApi
) : PagingSource<String, ChannelSearchResponse>() {

    override fun getRefreshKey(state: PagingState<String, ChannelSearchResponse>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.pagination?.cursor
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, ChannelSearchResponse> {
        return try {
            val response = helixApi.getChannels(
                query = query,
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
