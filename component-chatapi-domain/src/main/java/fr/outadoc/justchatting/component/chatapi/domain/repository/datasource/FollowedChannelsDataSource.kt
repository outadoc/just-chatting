package fr.outadoc.justchatting.component.chatapi.domain.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.component.chatapi.domain.model.Follow
import fr.outadoc.justchatting.component.chatapi.domain.model.FollowResponse
import fr.outadoc.justchatting.component.chatapi.domain.model.Pagination
import fr.outadoc.justchatting.component.twitch.http.api.HelixApi

class FollowedChannelsDataSource(
    private val userId: String?,
    private val helixApi: HelixApi,
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
                offset = params.key,
            )

            LoadResult.Page(
                data = listOf(
                    FollowResponse(
                        total = response.total,
                        data = response.data?.map { follow ->
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
                        pagination = Pagination(
                            cursor = response.pagination?.cursor,
                        ),
                    ),
                ),
                nextKey = response.pagination?.cursor,
                prevKey = null,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
