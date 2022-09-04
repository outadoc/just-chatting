package fr.outadoc.justchatting.repository.datasource

import androidx.paging.DataSource
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.model.helix.stream.Stream
import kotlinx.coroutines.CoroutineScope

class FollowedStreamsDataSource(
    private val userId: String?,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixApi: HelixApi,
    coroutineScope: CoroutineScope
) : BasePositionalDataSource<Stream>(coroutineScope) {

    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        if (helixToken.isNullOrBlank()) return

        loadInitial(params, callback) {
            helixApi.getFollowedStreams(
                clientId = helixClientId,
                token = helixToken,
                userId = userId,
                limit = 100,
                offset = offset
            ).also { offset = it.pagination?.cursor }
                .data
                .orEmpty()
                .associateBy { stream -> stream.user_id }
                .values
                .mapWithUserProfileImages()
                .sortedByDescending { stream ->
                    stream.viewer_count
                }
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        if (offset.isNullOrBlank()) return

        loadRange(params, callback) {
            helixApi.getFollowedStreams(
                clientId = helixClientId,
                token = helixToken,
                userId = userId,
                limit = 100,
                offset = offset
            )
                .data
                .orEmpty()
                .mapWithUserProfileImages()
        }
    }

    private suspend fun Collection<Stream>.mapWithUserProfileImages(): List<Stream> {
        val users = mapNotNull { it.user_id }
            .chunked(100)
            .flatMap { ids ->
                helixApi.getUsersById(
                    clientId = helixClientId,
                    token = helixToken,
                    ids = ids
                )
                    .data
                    .orEmpty()
            }

        return map { stream ->
            val user = users.firstOrNull { user -> stream.user_id == user.id }
            stream.copy(
                profileImageURL = user?.profile_image_url
            )
        }
    }

    class Factory(
        private val userId: String?,
        private val helixClientId: String?,
        private val helixToken: String?,
        private val helixApi: HelixApi,
        private val coroutineScope: CoroutineScope
    ) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
            FollowedStreamsDataSource(
                userId = userId,
                helixClientId = helixClientId,
                helixToken = helixToken,
                helixApi = helixApi,
                coroutineScope = coroutineScope
            ).also(sourceLiveData::postValue)
    }
}
