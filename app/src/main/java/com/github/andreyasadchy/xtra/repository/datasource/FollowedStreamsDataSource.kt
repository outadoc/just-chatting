package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.LocalFollowChannelRepository
import kotlinx.coroutines.CoroutineScope

class FollowedStreamsDataSource(
    private val localFollowsChannel: LocalFollowChannelRepository,
    private val userId: String?,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixApi: HelixApi,
    coroutineScope: CoroutineScope
) : BasePositionalDataSource<Stream>(coroutineScope) {

    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        check(!helixToken.isNullOrBlank())

        loadInitial(params, callback) {
            val localIds = localFollowsChannel.loadFollows().map { it.user_id }

            val localStreams: Map<String?, Stream> =
                helixLocal(localIds)
                    .associateBy { stream -> stream.user_id }

            val remoteStreams: Map<String?, Stream> =
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

            localStreams
                .plus(remoteStreams)
                .values
                .toList()
                .mapWithUserProfileImages()
                .sortedByDescending { stream ->
                    stream.viewer_count
                }
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            if (offset.isNullOrBlank()) emptyList()
            else helixApi.getFollowedStreams(
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

    private suspend fun List<Stream>.mapWithUserProfileImages(): List<Stream> {
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

    private suspend fun helixLocal(ids: List<String>): List<Stream> {
        val streams = ids.chunked(100)
            .flatMap { localIds ->
                helixApi.getStreams(
                    clientId = helixClientId,
                    token = helixToken,
                    ids = localIds
                )
                    .data
                    .orEmpty()
                    .filter { it.viewer_count != null }
            }

        val users = streams.mapNotNull { it.user_id }
            .chunked(100)
            .flatMap { streamIds ->
                helixApi.getUsersById(
                    clientId = helixClientId,
                    token = helixToken,
                    ids = streamIds
                )
                    .data
                    .orEmpty()
            }

        return streams.map { stream ->
            val user = users.find { it.id == stream.user_id }
            stream.copy(
                profileImageURL = user?.profile_image_url
            )
        }
    }

    class Factory(
        private val localFollowsChannel: LocalFollowChannelRepository,
        private val userId: String?,
        private val helixClientId: String?,
        private val helixToken: String?,
        private val helixApi: HelixApi,
        private val coroutineScope: CoroutineScope
    ) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
            FollowedStreamsDataSource(
                localFollowsChannel = localFollowsChannel,
                userId = userId,
                helixClientId = helixClientId,
                helixToken = helixToken,
                helixApi = helixApi,
                coroutineScope = coroutineScope
            ).also(sourceLiveData::postValue)
    }
}
