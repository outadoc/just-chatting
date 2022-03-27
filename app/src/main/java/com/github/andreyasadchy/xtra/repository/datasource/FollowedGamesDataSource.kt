package com.github.andreyasadchy.xtra.repository.datasource

import androidx.core.util.Pair
import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.FollowedGamesQuery
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientWithTokenFactory.apolloClientWithToken
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.LocalFollowGameRepository
import com.github.andreyasadchy.xtra.util.C
import kotlinx.coroutines.CoroutineScope

class FollowedGamesDataSource(
    private val localFollowsGame: LocalFollowGameRepository,
    private val userId: String?,
    private val gqlClientId: String?,
    private val gqlToken: String?,
    private val gqlApi: GraphQLRepository,
    private val apiPref: ArrayList<Pair<Long?, String?>?>,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Game>(coroutineScope) {
    private var api: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Game>) {
        loadInitial(params, callback) {
            val list = mutableListOf<Game>()
            for (i in localFollowsGame.loadFollows()) {
                list.add(Game(id = i.game_id, name = i.game_name, box_art_url = i.boxArt, followLocal = true))
            }
            val remote = try {
                when (apiPref.elementAt(0)?.second) {
                    C.GQL_QUERY -> if (!gqlToken.isNullOrBlank()) gqlQueryInitial(params) else throw Exception()
                    C.GQL -> if (!gqlToken.isNullOrBlank()) gqlInitial(params) else throw Exception()
                    else -> mutableListOf()
                }
            } catch (e: Exception) {
                try {
                    when (apiPref.elementAt(1)?.second) {
                        C.GQL_QUERY -> if (!gqlToken.isNullOrBlank()) gqlQueryInitial(params) else throw Exception()
                        C.GQL -> if (!gqlToken.isNullOrBlank()) gqlInitial(params) else throw Exception()
                        else -> mutableListOf()
                    }
                } catch (e: Exception) {
                    mutableListOf()
                }
            }
            if (!remote.isNullOrEmpty()) {
                for (i in remote) {
                    val item = list.find { it.id == i.id }
                    if (item == null) {
                        i.followTwitch = true
                        list.add(i)
                    } else {
                        item.followTwitch = true
                        item.viewersCount = i.viewersCount
                        item.broadcastersCount = i.broadcastersCount
                        item.tags = i.tags
                    }
                }
            }
            list.sortBy { it.name }
            list
        }
    }

    private suspend fun gqlQueryInitial(params: LoadInitialParams): List<Game> {
        api = C.GQL_QUERY
        val get1 = apolloClientWithToken(XtraModule(), gqlClientId, gqlToken)
            .query(FollowedGamesQuery(id = Optional.Present(userId), first = Optional.Present(100))).execute().data?.user?.followedGames
        val get = get1?.nodes
        val list = mutableListOf<Game>()
        if (get != null) {
            for (i in get) {
                list.add(
                    Game(
                        id = i?.id,
                        name = i?.displayName,
                        box_art_url = i?.boxArtURL,
                        viewersCount = i?.viewersCount,
                        broadcastersCount = i?.broadcastersCount
                    )
                )
            }
        }
        return list
    }

    private suspend fun gqlInitial(params: LoadInitialParams): List<Game> {
        api = C.GQL
        val get = gqlApi.loadFollowedGames(gqlClientId, gqlToken, 100)
        return get.data
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Game>) {
        loadRange(params, callback) {
            mutableListOf()
        }
    }

    class Factory(
        private val localFollowsGame: LocalFollowGameRepository,
        private val userId: String?,
        private val gqlClientId: String?,
        private val gqlToken: String?,
        private val gqlApi: GraphQLRepository,
        private val apiPref: ArrayList<Pair<Long?, String?>?>,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Game, FollowedGamesDataSource>() {

        override fun create(): DataSource<Int, Game> =
                FollowedGamesDataSource(localFollowsGame, userId, gqlClientId, gqlToken, gqlApi, apiPref, coroutineScope).also(sourceLiveData::postValue)
    }
}
