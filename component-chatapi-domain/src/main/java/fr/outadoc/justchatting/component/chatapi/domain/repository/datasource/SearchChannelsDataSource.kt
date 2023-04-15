package fr.outadoc.justchatting.component.chatapi.domain.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearch
import fr.outadoc.justchatting.component.chatapi.domain.model.Pagination
import fr.outadoc.justchatting.component.twitch.http.api.HelixApi

class SearchChannelsDataSource(
    private val query: String,
    private val helixApi: HelixApi,
) : PagingSource<Pagination, List<ChannelSearch>>() {

    override fun getRefreshKey(state: PagingState<Pagination, List<ChannelSearch>>): Pagination? =
        null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, List<ChannelSearch>> {
        return try {
            val response = helixApi.searchChannels(
                query = query,
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
                    response.data.map { search ->
                        ChannelSearch(
                            id = search.id,
                            title = search.title,
                            broadcasterLogin = search.userLogin,
                            broadcasterDisplayName = search.userDisplayName,
                            broadcasterLanguage = search.broadcasterLanguage,
                            gameId = search.gameId,
                            gameName = search.gameName,
                            isLive = search.isLive,
                            startedAt = search.startedAt,
                            thumbnailUrl = search.thumbnailUrl,
                            tags = search.tags,
                        )
                    },
                ),
                prevKey = null,
                nextKey = response.pagination.cursor?.let { cursor -> Pagination.Next(cursor) },
                itemsAfter = itemsAfter,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
