package fr.outadoc.justchatting.component.chatapi.domain.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelFollow
import fr.outadoc.justchatting.component.chatapi.domain.model.Pagination
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.component.twitch.http.api.HelixApi
import fr.outadoc.justchatting.utils.logging.logError

class FollowedChannelsDataSource(
    private val userId: String?,
    private val helixApi: HelixApi,
) : PagingSource<Pagination, List<ChannelFollow>>() {

    override fun getRefreshKey(state: PagingState<Pagination, List<ChannelFollow>>): Pagination? =
        null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, List<ChannelFollow>> {
        return helixApi
            .getFollowedChannels(
                userId = userId,
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
                        data = listOf(
                            response.data.map { follow ->
                                ChannelFollow(
                                    user = User(
                                        id = follow.userId,
                                        login = follow.userLogin,
                                        displayName = follow.userDisplayName,
                                    ),
                                    followedAt = follow.followedAt,
                                )
                            },
                        ),
                        prevKey = null,
                        nextKey = response.pagination.cursor?.let { cursor -> Pagination.Next(cursor) },
                        itemsAfter = itemsAfter,
                    )
                },
                onFailure = { exception ->
                    logError<FollowedChannelsDataSource>(exception) { "Error while fetching followed streams" }
                    LoadResult.Error(exception)
                },
            )
    }
}
