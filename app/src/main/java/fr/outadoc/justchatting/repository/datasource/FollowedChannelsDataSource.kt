package fr.outadoc.justchatting.repository.datasource

import androidx.paging.DataSource
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.model.helix.follows.Order
import fr.outadoc.justchatting.model.helix.follows.Sort
import fr.outadoc.justchatting.model.helix.user.User
import kotlinx.coroutines.CoroutineScope

class FollowedChannelsDataSource(
    private val userId: String?,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixApi: HelixApi,
    private val sort: Sort,
    private val order: Order,
    private val coroutineScope: CoroutineScope
) : BasePositionalDataSource<Follow>(coroutineScope) {

    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Follow>) {
        if (helixToken.isNullOrBlank()) return

        loadInitial(params, callback) {
            val list: Collection<Follow> =
                helixApi.getFollowedChannels(
                    clientId = helixClientId,
                    token = helixToken,
                    userId = userId,
                    limit = 100,
                    offset = offset
                )
                    .also { offset = it.pagination?.cursor }
                    .data
                    .orEmpty()
                    .associateBy { it.to_id }
                    .values
                    .mapWithUserProfileImages()

            when (order) {
                Order.ASC -> when (sort) {
                    Sort.FOLLOWED_AT -> list.sortedBy { it.followed_at }
                    else -> list.sortedBy { it.to_login }
                }
                Order.DESC -> when (sort) {
                    Sort.FOLLOWED_AT -> list.sortedByDescending { it.followed_at }
                    else -> list.sortedByDescending { it.to_login }
                }
            }
        }
    }

    private suspend fun Collection<Follow>.mapWithUserProfileImages(): Collection<Follow> {
        val results: List<User> =
            filter { follow ->
                follow.profileImageURL == null ||
                        follow.profileImageURL.contains("image_manager_disk_cache") ||
                        follow.lastBroadcast == null
            }
                .mapNotNull { follow -> follow.to_id }
                .chunked(size = 100)
                .flatMap { idsToUpdate ->
                    helixApi.getUsersById(
                        clientId = helixClientId,
                        token = helixToken,
                        ids = idsToUpdate
                    )
                        .data
                        .orEmpty()
                }

        return map { follow ->
            val userInfo = results.firstOrNull { user -> user.id == follow.to_id }
            follow.copy(
                profileImageURL = userInfo?.profile_image_url
            )
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Follow>) {
        check(!helixToken.isNullOrBlank())
        if (offset.isNullOrBlank()) return

        loadRange(params, callback) {
            helixApi.getFollowedChannels(
                clientId = helixClientId,
                token = helixToken,
                userId = userId,
                limit = 100,
                offset = offset
            )
                .also { offset = it.pagination?.cursor }
                .data
                .orEmpty()
                .mapWithUserProfileImages()
                .toList()
        }
    }

    class Factory(
        private val userId: String?,
        private val helixClientId: String?,
        private val helixToken: String?,
        private val helixApi: HelixApi,
        private val sort: Sort,
        private val order: Order,
        private val coroutineScope: CoroutineScope
    ) : BaseDataSourceFactory<Int, Follow, FollowedChannelsDataSource>() {

        override fun create(): DataSource<Int, Follow> =
            FollowedChannelsDataSource(
                userId = userId,
                helixClientId = helixClientId,
                helixToken = helixToken,
                helixApi = helixApi,
                sort = sort,
                order = order,
                coroutineScope = coroutineScope
            ).also(sourceLiveData::postValue)
    }
}
