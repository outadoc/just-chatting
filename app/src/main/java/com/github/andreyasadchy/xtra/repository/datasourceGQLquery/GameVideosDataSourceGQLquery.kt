package com.github.andreyasadchy.xtra.repository.datasourceGQLquery

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.GameVideosQuery
import com.github.andreyasadchy.xtra.apolloClient
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import com.github.andreyasadchy.xtra.type.VideoSort
import kotlinx.coroutines.CoroutineScope

class GameVideosDataSourceGQLquery private constructor(
    private val clientId: String?,
    private val game: String?,
    private val type: String?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Video>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadInitial(params, callback) {
            val get = apolloClient(clientId).query(GameVideosQuery(Optional.Present(game), Optional.Present(VideoSort.VIEWS), Optional.Present(params.requestedLoadSize), Optional.Present(offset))).execute().data?.game?.videos?.edges
            val list = mutableListOf<Video>()
            if (get != null) {
                for (i in get) {
                    list.add(
                        Video(
                            id = i?.node?.id ?: "",
                            user_id = i?.node?.owner?.id,
                            user_login = i?.node?.owner?.login,
                            user_name = i?.node?.owner?.displayName,
                            game_name = game,
                            type = i?.node?.broadcastType.toString(),
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            createdAt = i?.node?.createdAt,
                            duration = i?.node?.lengthSeconds.toString(),
                            thumbnail_url = i?.node?.previewThumbnailURL,
                            profileImageURL = i?.node?.owner?.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        loadRange(params, callback) {
            val get = apolloClient(clientId).query(GameVideosQuery(Optional.Present(game), Optional.Present(VideoSort.VIEWS), Optional.Present(params.loadSize), Optional.Present(offset))).execute().data?.game?.videos?.edges
            val list = mutableListOf<Video>()
            if (get != null) {
                for (i in get) {
                    list.add(
                        Video(
                            id = i?.node?.id ?: "",
                            user_id = i?.node?.owner?.id,
                            user_login = i?.node?.owner?.login,
                            user_name = i?.node?.owner?.displayName,
                            game_name = game,
                            type = i?.node?.broadcastType.toString(),
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            createdAt = i?.node?.createdAt,
                            duration = i?.node?.lengthSeconds.toString(),
                            thumbnail_url = i?.node?.previewThumbnailURL,
                            profileImageURL = i?.node?.owner?.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
            }
            list
        }
    }

    class Factory (
        private val clientId: String?,
        private val game: String?,
        private val type: String?,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Video, GameVideosDataSourceGQLquery>() {

        override fun create(): DataSource<Int, Video> =
                GameVideosDataSourceGQLquery(clientId, game, type, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
