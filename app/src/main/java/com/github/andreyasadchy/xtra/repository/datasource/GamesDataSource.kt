package com.github.andreyasadchy.xtra.repository.datasource

import androidx.core.util.Pair
import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.TopGamesQuery
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.util.C
import kotlinx.coroutines.CoroutineScope

class GamesDataSource(
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixApi: HelixApi,
    private val gqlClientId: String?,
    private val tags: List<String>?,
    private val gqlApi: GraphQLRepository,
    private val apiPref: ArrayList<Pair<Long?, String?>?>,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Game>(coroutineScope) {
    private var api: String? = null
    private var offset: String? = null
    private var nextPage: Boolean = true

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Game>) {
        loadInitial(params, callback) {
            try {
                when (apiPref.elementAt(0)?.second) {
                    C.HELIX -> if (!helixToken.isNullOrBlank() && tags.isNullOrEmpty()) helixInitial(params) else throw Exception()
                    C.GQL_QUERY -> if (tags.isNullOrEmpty()) gqlQueryInitial(params) else throw Exception()
                    C.GQL -> gqlInitial(params)
                    else -> mutableListOf()
                }
            } catch (e: Exception) {
                try {
                    when (apiPref.elementAt(1)?.second) {
                        C.HELIX -> if (!helixToken.isNullOrBlank() && tags.isNullOrEmpty()) helixInitial(params) else throw Exception()
                        C.GQL_QUERY -> if (tags.isNullOrEmpty()) gqlQueryInitial(params) else throw Exception()
                        C.GQL -> gqlInitial(params)
                        else -> mutableListOf()
                    }
                } catch (e: Exception) {
                    try {
                        when (apiPref.elementAt(2)?.second) {
                            C.HELIX -> if (!helixToken.isNullOrBlank() && tags.isNullOrEmpty()) helixInitial(params) else throw Exception()
                            C.GQL_QUERY -> if (tags.isNullOrEmpty()) gqlQueryInitial(params) else throw Exception()
                            C.GQL -> gqlInitial(params)
                            else -> mutableListOf()
                        }
                    } catch (e: Exception) {
                        mutableListOf()
                    }
                }
            }
        }
    }

    private suspend fun helixInitial(params: LoadInitialParams): List<Game> {
        api = C.HELIX
        val get = helixApi.getTopGames(helixClientId, helixToken, params.requestedLoadSize, offset)
        return if (get.data != null) {
            offset = get.pagination?.cursor
            get.data
        } else mutableListOf()
    }

    private suspend fun gqlQueryInitial(params: LoadInitialParams): List<Game> {
        api = C.GQL_QUERY
        val get1 = XtraModule_ApolloClientFactory.apolloClient(XtraModule(), gqlClientId)
            .query(TopGamesQuery(first = Optional.Present(params.requestedLoadSize), after = Optional.Present(offset))).execute().data?.games
        val get = get1?.edges
        val list = mutableListOf<Game>()
        if (get != null) {
            for (i in get) {
                list.add(
                    Game(
                        id = i?.node?.id,
                        name = i?.node?.displayName,
                        box_art_url = i?.node?.boxArtURL,
                        viewersCount = i?.node?.viewersCount,
                        broadcastersCount = i?.node?.broadcastersCount,
                    )
                )
            }
            offset = get.lastOrNull()?.cursor
            nextPage = get1.pageInfo?.hasNextPage ?: true
        }
        return list
    }

    private suspend fun gqlInitial(params: LoadInitialParams): List<Game> {
        api = C.GQL
        val get = gqlApi.loadTopGames(gqlClientId, tags, params.requestedLoadSize, offset)
        offset = get.cursor
        return get.data
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Game>) {
        loadRange(params, callback) {
            when (api) {
                C.HELIX -> helixRange(params)
                C.GQL_QUERY -> gqlQueryRange(params)
                C.GQL -> gqlRange(params)
                else -> mutableListOf()
            }
        }
    }

    private suspend fun helixRange(params: LoadRangeParams): List<Game> {
        val get = helixApi.getTopGames(helixClientId, helixToken, params.loadSize, offset)
        return if (offset != null && offset != "") {
            if (get.data != null) {
                offset = get.pagination?.cursor
                get.data
            } else mutableListOf()
        } else mutableListOf()
    }

    private suspend fun gqlQueryRange(params: LoadRangeParams): List<Game> {
        val get1 = XtraModule_ApolloClientFactory.apolloClient(XtraModule(), gqlClientId)
            .query(TopGamesQuery(first = Optional.Present(params.loadSize), after = Optional.Present(offset))).execute().data?.games
        val get = get1?.edges
        val list = mutableListOf<Game>()
        if (get != null && nextPage && offset != null && offset != "") {
            for (i in get) {
                list.add(
                    Game(
                        id = i?.node?.id,
                        name = i?.node?.displayName,
                        box_art_url = i?.node?.boxArtURL,
                        viewersCount = i?.node?.viewersCount,
                        broadcastersCount = i?.node?.broadcastersCount,
                    )
                )
            }
            offset = get.lastOrNull()?.cursor
            nextPage = get1.pageInfo?.hasNextPage ?: true
        }
        return list
    }

    private suspend fun gqlRange(params: LoadRangeParams): List<Game> {
        val get = gqlApi.loadTopGames(gqlClientId, tags, params.loadSize, offset)
        return if (offset != null && offset != "") {
            offset = get.cursor
            get.data
        } else mutableListOf()
    }

    class Factory(
        private val helixClientId: String?,
        private val helixToken: String?,
        private val helixApi: HelixApi,
        private val gqlClientId: String?,
        private val tags: List<String>?,
        private val gqlApi: GraphQLRepository,
        private val apiPref: ArrayList<Pair<Long?, String?>?>,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Game, GamesDataSource>() {

        override fun create(): DataSource<Int, Game> = GamesDataSource(helixClientId, helixToken, helixApi, gqlClientId, tags, gqlApi, apiPref, coroutineScope).also(sourceLiveData::postValue)
    }
}
