package com.github.andreyasadchy.xtra.repository.datasourceGQLquery

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.UserVideosQuery
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import com.github.andreyasadchy.xtra.type.BroadcastType
import com.github.andreyasadchy.xtra.type.VideoSort
import kotlinx.coroutines.CoroutineScope

class ChannelVideosDataSourceGQLquery private constructor(
    private val clientId: String?,
    private val channelId: String?,
    private val type: BroadcastType?,
    private val sort: VideoSort?,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Video>(coroutineScope) {
    private var offset: String? = null
    private var nextPage: Boolean = true

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadInitial(params, callback) {
            val get1 = apolloClient(XtraModule(), clientId).query(UserVideosQuery(id = Optional.Present(channelId), sort = Optional.Present(sort), type = Optional.Present(type), first = Optional.Present(params.requestedLoadSize), after = Optional.Present(offset))).execute().data?.user
            val get = get1?.videos?.edges
            val list = mutableListOf<Video>()
            if (get != null) {
                for (i in get) {
                    list.add(
                        Video(
                            id = i?.node?.id ?: "",
                            user_id = channelId,
                            user_login = get1.login,
                            user_name = get1.displayName,
                            gameId = i?.node?.game?.id,
                            gameName = i?.node?.game?.displayName,
                            type = i?.node?.broadcastType.toString(),
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            createdAt = i?.node?.createdAt,
                            duration = i?.node?.lengthSeconds.toString(),
                            thumbnail_url = i?.node?.previewThumbnailURL,
                            profileImageURL = get1.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
                nextPage = get1.videos.pageInfo?.hasNextPage ?: true
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        loadRange(params, callback) {
            val get1 = apolloClient(XtraModule(), clientId).query(UserVideosQuery(id = Optional.Present(channelId), sort = Optional.Present(sort), type = Optional.Present(type), first = Optional.Present(params.loadSize), after = Optional.Present(offset))).execute().data?.user
            val get = get1?.videos?.edges
            val list = mutableListOf<Video>()
            if (get != null && nextPage && offset != null && offset != "") {
                for (i in get) {
                    list.add(
                        Video(
                            id = i?.node?.id ?: "",
                            user_id = channelId,
                            user_login = get1.login,
                            user_name = get1.displayName,
                            gameId = i?.node?.game?.id,
                            gameName = i?.node?.game?.displayName,
                            type = i?.node?.broadcastType.toString(),
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            createdAt = i?.node?.createdAt,
                            duration = i?.node?.lengthSeconds.toString(),
                            thumbnail_url = i?.node?.previewThumbnailURL,
                            profileImageURL = get1.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
                nextPage = get1.videos.pageInfo?.hasNextPage ?: true
            }
            list
        }
    }

    class Factory (
        private val clientId: String?,
        private val channelId: String?,
        private val type: BroadcastType?,
        private val sort: VideoSort?,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Video, ChannelVideosDataSourceGQLquery>() {

        override fun create(): DataSource<Int, Video> =
                ChannelVideosDataSourceGQLquery(clientId, channelId, type, sort, coroutineScope).also(sourceLiveData::postValue)
    }
}
