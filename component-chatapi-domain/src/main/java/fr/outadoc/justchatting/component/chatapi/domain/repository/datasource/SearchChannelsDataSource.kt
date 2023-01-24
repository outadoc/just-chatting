package fr.outadoc.justchatting.component.chatapi.domain.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearch
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearchResponse
import fr.outadoc.justchatting.component.chatapi.domain.model.Pagination
import fr.outadoc.justchatting.component.twitch.api.HelixApi

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
                data = listOf(
                    ChannelSearchResponse(
                        data = response.data?.map { search ->
                            ChannelSearch(
                                id = search.id,
                                title = search.title,
                                broadcasterLogin = search.broadcasterLogin,
                                broadcasterDisplayName = search.broadcasterDisplayName,
                                broadcasterLanguage = search.broadcasterLanguage,
                                gameId = search.gameId,
                                gameName = search.gameName,
                                isLive = search.isLive,
                                startedAt = search.startedAt,
                                thumbnailUrl = search.thumbnailUrl,
                                profileImageURL = search.profileImageURL,
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
