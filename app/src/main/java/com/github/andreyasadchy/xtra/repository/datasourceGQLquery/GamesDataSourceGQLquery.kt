package com.github.andreyasadchy.xtra.repository.datasourceGQLquery

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.TopGamesQuery
import com.github.andreyasadchy.xtra.apolloClient
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import kotlinx.coroutines.CoroutineScope

class GamesDataSourceGQLquery(
    private val clientId: String?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Game>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Game>) {
        loadInitial(params, callback) {
            val get = apolloClient(clientId).query(TopGamesQuery(Optional.Present(params.requestedLoadSize), Optional.Present(offset))).execute().data?.games?.edges
            val list = mutableListOf<Game>()
            if (get != null) {
                for (i in get) {
                    list.add(
                        Game(
                        name = i?.node?.displayName,
                        box_art_url = i?.node?.avatarURL,
                        viewersCount = i?.node?.viewersCount,
                        broadcastersCount = i?.node?.broadcastersCount,
                    )
                    )
                }
                offset = get.lastOrNull()?.cursor
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Game>) {
        loadRange(params, callback) {
            val get = apolloClient(clientId).query(TopGamesQuery(Optional.Present(params.loadSize), Optional.Present(offset))).execute().data?.games?.edges
            val list = mutableListOf<Game>()
            if (get != null) {
                for (i in get) {
                    list.add(
                        Game(
                            name = i?.node?.displayName,
                            box_art_url = i?.node?.avatarURL,
                            viewersCount = i?.node?.viewersCount,
                            broadcastersCount = i?.node?.broadcastersCount,
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
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Game, GamesDataSourceGQLquery>() {

        override fun create(): DataSource<Int, Game> = GamesDataSourceGQLquery(clientId, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
