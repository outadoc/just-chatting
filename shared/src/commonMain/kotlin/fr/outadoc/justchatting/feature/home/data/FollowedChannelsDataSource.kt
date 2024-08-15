package fr.outadoc.justchatting.feature.home.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.Pagination
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.datetime.Instant

internal class FollowedChannelsDataSource(
    private val userId: String?,
    private val twitchClient: TwitchClient,
) : PagingSource<Pagination, List<ChannelFollow>>() {

    override fun getRefreshKey(state: PagingState<Pagination, List<ChannelFollow>>): Pagination? =
        null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, List<ChannelFollow>> {
        return twitchClient
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
                                        // TODO replace paging with db
                                        description = "",
                                        profileImageUrl = "",
                                        createdAt = Instant.DISTANT_PAST,
                                        usedAt = Instant.DISTANT_PAST,
                                    ),
                                    followedAt = Instant.parse(follow.followedAt),
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
