package fr.outadoc.justchatting.component.chatapi.domain.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelFollow
import fr.outadoc.justchatting.component.chatapi.domain.model.Pagination
import fr.outadoc.justchatting.component.twitch.http.api.HelixApi
import fr.outadoc.justchatting.component.twitch.http.model.FollowResponse

class FollowedChannelsDataSource(
    private val userId: String?,
    private val helixApi: HelixApi,
) : PagingSource<Pagination, List<ChannelFollow>>() {

    override fun getRefreshKey(state: PagingState<Pagination, List<ChannelFollow>>): Pagination? =
        null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, List<ChannelFollow>> {
        return try {
            val after: String? = (params.key as? Pagination.Next)?.cursor

            val response: FollowResponse =
                helixApi.getFollowedChannels(
                    userId = userId,
                    limit = params.loadSize,
                    after = after,
                )

            val itemsAfter: Int =
                if (response.pagination.cursor == null) {
                    0
                } else {
                    LoadResult.Page.COUNT_UNDEFINED
                }

            LoadResult.Page(
                data = listOf(
                    response.data.map { follow ->
                        ChannelFollow(
                            userId = follow.userId,
                            userLogin = follow.userLogin,
                            userDisplayName = follow.userDisplayName,
                            followedAt = follow.followedAt,
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
