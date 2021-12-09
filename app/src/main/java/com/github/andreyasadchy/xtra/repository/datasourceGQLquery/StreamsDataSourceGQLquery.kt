package com.github.andreyasadchy.xtra.repository.datasourceGQLquery

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.TopStreamsQuery
import com.github.andreyasadchy.xtra.apolloClient
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import kotlinx.coroutines.CoroutineScope

class StreamsDataSourceGQLquery private constructor(
    private val clientId: String?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            val get = apolloClient(clientId).query(TopStreamsQuery(Optional.Present(params.requestedLoadSize), Optional.Present(offset))).execute().data?.streams?.edges
            val list = mutableListOf<Stream>()
            if (get != null) {
                for (i in get) {
                    list.add(Stream(
                        id = i?.node?.id,
                        user_id = i?.node?.broadcaster?.id,
                        user_login = i?.node?.broadcaster?.login,
                        user_name = i?.node?.broadcaster?.displayName,
                        game_name = i?.node?.game?.name,
                        type = i?.node?.type,
                        title = i?.node?.title,
                        viewer_count = i?.node?.viewersCount,
                        thumbnail_url = i?.node?.previewImageURL,
                        profileImageURL = i?.node?.broadcaster?.profileImageURL
                    ))
                }
                offset = get.lastOrNull()?.cursor
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            val get = apolloClient(clientId).query(TopStreamsQuery(Optional.Present(params.loadSize), Optional.Present(offset))).execute().data?.streams?.edges
            val list = mutableListOf<Stream>()
            if (get != null) {
                for (i in get) {
                    list.add(Stream(
                        id = i?.node?.id,
                        user_id = i?.node?.broadcaster?.id,
                        user_login = i?.node?.broadcaster?.login,
                        user_name = i?.node?.broadcaster?.displayName,
                        game_name = i?.node?.game?.name,
                        type = i?.node?.type,
                        title = i?.node?.title,
                        viewer_count = i?.node?.viewersCount,
                        thumbnail_url = i?.node?.previewImageURL,
                        profileImageURL = i?.node?.broadcaster?.profileImageURL
                    ))
                }
                offset = get.lastOrNull()?.cursor
            }
            list
        }
    }

    class Factory(
        private val clientId: String?,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, StreamsDataSourceGQLquery>() {

        override fun create(): DataSource<Int, Stream> =
                StreamsDataSourceGQLquery(clientId, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
