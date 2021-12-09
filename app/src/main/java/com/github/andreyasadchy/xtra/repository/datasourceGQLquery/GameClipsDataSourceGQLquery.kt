package com.github.andreyasadchy.xtra.repository.datasourceGQLquery

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.GameClipsQuery
import com.github.andreyasadchy.xtra.apolloClient
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import kotlinx.coroutines.CoroutineScope

class GameClipsDataSourceGQLquery(
    private val clientId: String?,
    private val game: String?,
    private val sort: ClipsPeriod?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Clip>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Clip>) {
        loadInitial(params, callback) {
            val get = apolloClient(clientId).query(GameClipsQuery(Optional.Present(game), Optional.Present(sort), Optional.Present(params.requestedLoadSize), Optional.Present(offset))).execute().data?.game?.clips?.edges
            val list = mutableListOf<Clip>()
            if (get != null) {
                for (i in get) {
                    list.add(
                        Clip(
                            id = i?.node?.slug ?: "",
                            broadcaster_id = i?.node?.broadcaster?.id,
                            broadcaster_login = i?.node?.broadcaster?.login,
                            broadcaster_name = i?.node?.broadcaster?.displayName,
                            game_name = game,
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            created_at = i?.node?.createdAt,
                            duration = i?.node?.durationSeconds?.toDouble(),
                            thumbnail_url = i?.node?.thumbnailURL,
                            profileImageURL = i?.node?.broadcaster?.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Clip>) {
        loadRange(params, callback) {
            val get = apolloClient(clientId).query(GameClipsQuery(Optional.Present(game), Optional.Present(sort), Optional.Present(params.loadSize), Optional.Present(offset))).execute().data?.game?.clips?.edges
            val list = mutableListOf<Clip>()
            if (get != null) {
                for (i in get) {
                    list.add(
                        Clip(
                            id = i?.node?.slug ?: "",
                            broadcaster_id = i?.node?.broadcaster?.id,
                            broadcaster_login = i?.node?.broadcaster?.login,
                            broadcaster_name = i?.node?.broadcaster?.displayName,
                            game_name = game,
                            title = i?.node?.title,
                            view_count = i?.node?.viewCount,
                            created_at = i?.node?.createdAt,
                            duration = i?.node?.durationSeconds?.toDouble(),
                            thumbnail_url = i?.node?.thumbnailURL,
                            profileImageURL = i?.node?.broadcaster?.profileImageURL,
                        )
                    )
                }
                offset = get.lastOrNull()?.cursor
            }
            list
        }
    }

    class Factory(
        private val clientId: String?,
        private val game: String?,
        private val sort: ClipsPeriod?,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Clip, GameClipsDataSourceGQLquery>() {

        override fun create(): DataSource<Int, Clip> =
                GameClipsDataSourceGQLquery(clientId, game, sort, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
