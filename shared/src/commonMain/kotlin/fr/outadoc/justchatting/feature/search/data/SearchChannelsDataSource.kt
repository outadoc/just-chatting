package fr.outadoc.justchatting.feature.search.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.data.TwitchClient
import fr.outadoc.justchatting.feature.shared.domain.model.Pagination
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Instant

internal class SearchChannelsDataSource(
    private val query: String,
    private val twitchClient: TwitchClient,
) : PagingSource<Pagination, List<ChannelSearchResult>>() {
    /**
     * Twitch paginates search results by offset (the cursor is a base64-encoded offset), over a
     * result set that is re-ranked live as channels go on and off air. A channel that moves down
     * the ranking while we're scrolling gets served a second time at a later offset, which would
     * then break the uniqueness of the keys the list UI derives from the user id.
     *
     * Keep track of what we've already emitted so that each user is only ever handed over once.
     */
    private val seenUserIdsLock = Mutex()
    private val seenUserIds = mutableSetOf<String>()

    override fun getRefreshKey(state: PagingState<Pagination, List<ChannelSearchResult>>): Pagination? = null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, List<ChannelSearchResult>> {
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
            ).fold(
                onSuccess = { response ->
                    val itemsAfter: Int =
                        if (response.pagination.cursor == null) {
                            0
                        } else {
                            LoadResult.Page.COUNT_UNDEFINED
                        }

                    val results =
                        response.data.map { search ->
                            ChannelSearchResult(
                                title = search.title,
                                user =
                                User(
                                    id = search.userId,
                                    login = search.userLogin,
                                    displayName = search.userDisplayName,
                                    description = "",
                                    profileImageUrl = "",
                                    createdAt = Instant.DISTANT_PAST,
                                    usedAt = Instant.DISTANT_PAST,
                                ),
                                language = search.broadcasterLanguage,
                                gameId = search.gameId,
                                gameName = search.gameName,
                                isLive = search.isLive,
                                thumbnailUrl = search.thumbnailUrl,
                                tags = search.tags.toPersistentList(),
                            )
                        }

                    val newResults =
                        seenUserIdsLock.withLock {
                            results.filter { result -> seenUserIds.add(result.user.id) }
                        }

                    LoadResult.Page(
                        data = listOf(newResults),
                        prevKey = null,
                        nextKey =
                        response.pagination.cursor?.let { cursor ->
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
