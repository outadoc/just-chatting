package fr.outadoc.justchatting.component.chatapi.domain.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.component.chatapi.domain.model.Follow
import fr.outadoc.justchatting.component.chatapi.domain.model.Pagination
import fr.outadoc.justchatting.component.twitch.http.api.HelixApi

class FollowedChannelsDataSource(
    private val userId: String?,
    private val helixApi: HelixApi,
) : PagingSource<Pagination, List<Follow>>() {

    override fun getRefreshKey(state: PagingState<Pagination, List<Follow>>): Pagination? = null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, List<Follow>> {
        return try {
            val response = when (val key = params.key) {
                is Pagination.Next ->
                    helixApi.getFollowedChannels(
                        userId = userId,
                        limit = params.loadSize,
                        after = key.cursor,
                    )

                is Pagination.Previous -> {
                    helixApi.getFollowedChannels(
                        userId = userId,
                        limit = params.loadSize,
                        before = key.cursor,
                    )
                }

                null -> {
                    helixApi.getFollowedChannels(
                        userId = userId,
                        limit = params.loadSize,
                    )
                }
            }

            val isFirstPage = params.key == null
            val totalItemCount = response.total

            val otherItems: Int =
                when {
                    response.pagination.cursor == null -> 0

                    isFirstPage && totalItemCount != null ->
                        totalItemCount - response.data.orEmpty().size

                    else -> LoadResult.Page.COUNT_UNDEFINED
                }

            val itemsAfter: Int = when (params) {
                is LoadParams.Refresh, is LoadParams.Append -> otherItems
                is LoadParams.Prepend -> LoadResult.Page.COUNT_UNDEFINED
            }

            val itemsBefore: Int = when (params) {
                is LoadParams.Refresh, is LoadParams.Append -> LoadResult.Page.COUNT_UNDEFINED
                is LoadParams.Prepend -> otherItems
            }

            LoadResult.Page(
                data = listOf(
                    response.data.orEmpty()
                        .map { follow ->
                            Follow(
                                fromId = follow.fromId,
                                fromLogin = follow.fromLogin,
                                fromName = follow.fromName,
                                toId = follow.toId,
                                toLogin = follow.toLogin,
                                toName = follow.toName,
                                followedAt = follow.followedAt,
                            )
                        },
                ),
                nextKey = response.pagination.cursor?.let { cursor -> Pagination.Next(cursor) },
                prevKey = response.pagination.cursor?.let { cursor -> Pagination.Previous(cursor) },
                itemsBefore = itemsBefore,
                itemsAfter = itemsAfter,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
