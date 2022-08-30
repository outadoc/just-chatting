package fr.outadoc.justchatting.repository.datasource

import androidx.paging.DataSource
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.model.helix.channel.ChannelSearch
import kotlinx.coroutines.CoroutineScope

class SearchChannelsDataSource private constructor(
    private val query: String,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixApi: HelixApi,
    coroutineScope: CoroutineScope
) : BasePositionalDataSource<ChannelSearch>(coroutineScope) {

    private var offset: String? = null

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<ChannelSearch>
    ) {
        loadInitial(params, callback) {
            if (!helixToken.isNullOrBlank()) helixInitial(params)
            else throw Exception()
        }
    }

    private suspend fun helixInitial(params: LoadInitialParams): List<ChannelSearch> {
        val result = helixApi.getChannels(
            clientId = helixClientId,
            token = helixToken,
            query = query,
            limit = params.requestedLoadSize,
            offset = offset
        )

        offset = result.pagination?.cursor

        return result.data
            .orEmpty()
            .mapWithUserProfileImages()
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ChannelSearch>) {
        if (offset.isNullOrBlank()) return

        loadRange(params, callback) {
            val result = helixApi.getChannels(
                clientId = helixClientId,
                token = helixToken,
                query = query,
                limit = params.loadSize,
                offset = offset
            )

            offset = result.pagination?.cursor

            result.data
                .orEmpty()
                .mapWithUserProfileImages()
        }
    }

    private suspend fun List<ChannelSearch>.mapWithUserProfileImages(): List<ChannelSearch> {
        return mapNotNull { result -> result.id }
            .chunked(size = 100)
            .flatMap { idsToUpdate ->
                val users = helixApi.getUsersById(
                    clientId = helixClientId,
                    token = helixToken,
                    ids = idsToUpdate
                )
                    .data
                    .orEmpty()

                map { searchResult ->
                    searchResult.copy(
                        profileImageURL = users.firstOrNull { user -> user.id == searchResult.id }
                            ?.profile_image_url
                    )
                }
            }
    }

    class Factory(
        private val query: String,
        private val helixClientId: String?,
        private val helixToken: String?,
        private val helixApi: HelixApi,
        private val coroutineScope: CoroutineScope
    ) : BaseDataSourceFactory<Int, ChannelSearch, SearchChannelsDataSource>() {

        override fun create(): DataSource<Int, ChannelSearch> =
            SearchChannelsDataSource(
                query = query,
                helixClientId = helixClientId,
                helixToken = helixToken,
                helixApi = helixApi,
                coroutineScope = coroutineScope
            ).also(sourceLiveData::postValue)
    }
}
