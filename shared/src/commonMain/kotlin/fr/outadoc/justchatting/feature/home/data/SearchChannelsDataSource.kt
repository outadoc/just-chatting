package fr.outadoc.justchatting.feature.home.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.Pagination
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.utils.logging.logError

internal class SearchChannelsDataSource(
    private val query: String,
    private val twitchClient: TwitchClient,
) : PagingSource<Pagination, ChannelSearchResult>() {

    override fun getRefreshKey(state: PagingState<Pagination, ChannelSearchResult>): Pagination? =
        null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, ChannelSearchResult> {
        if (query.isBlank()) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null,
                itemsAfter = 0,
            )
        }

        return twitchClient
            .searchChannels(
                query = query,
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
                        data = response.data.map { search ->
                            ChannelSearchResult(
                                title = search.title,
                                user = User(
                                    id = search.userId,
                                    login = search.userLogin,
                                    displayName = search.userDisplayName,
                                ),
                                language = search.broadcasterLanguage,
                                gameId = search.gameId,
                                gameName = search.gameName,
                                isLive = search.isLive,
                                thumbnailUrl = search.thumbnailUrl,
                                tags = search.tags,
                            )
                        },
                        prevKey = null,
                        nextKey = response.pagination.cursor?.let { cursor ->
                            Pagination.Next(
                                cursor,
                            )
                        },
                        itemsAfter = itemsAfter,
                    )
                },
                onFailure = { exception ->
                    logError<SearchChannelsDataSource>(exception) { "Error while fetching followed streams" }
                    return LoadResult.Error(exception)
                },
            )
    }
}
