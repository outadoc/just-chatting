package com.github.andreyasadchy.xtra.repository.datasource

import androidx.core.util.Pair
import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.UserClipsQuery
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import com.github.andreyasadchy.xtra.util.C
import kotlinx.coroutines.CoroutineScope

class ChannelClipsDataSource(
    private val channelId: String?,
    private val channelLogin: String?,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val started_at: String?,
    private val ended_at: String?,
    private val helixApi: HelixApi,
    private val gqlClientId: String?,
    private val gqlQueryPeriod: ClipsPeriod?,
    private val gqlPeriod: String?,
    private val gqlApi: GraphQLRepository,
    private val apiPref: ArrayList<Pair<Long?, String?>?>,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Clip>(coroutineScope) {
    private var api: String? = null
    private var offset: String? = null
    private var nextPage: Boolean = true

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Clip>) {
        loadInitial(params, callback) {
            try {
                when (apiPref.elementAt(0)?.second) {
                    C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                    C.GQL_QUERY -> gqlQueryInitial(params)
                    C.GQL -> gqlInitial(params)
                    else -> mutableListOf()
                }
            } catch (e: Exception) {
                try {
                    when (apiPref.elementAt(1)?.second) {
                        C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                        C.GQL_QUERY -> gqlQueryInitial(params)
                        C.GQL -> gqlInitial(params)
                        else -> mutableListOf()
                    }
                } catch (e: Exception) {
                    try {
                        when (apiPref.elementAt(2)?.second) {
                            C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                            C.GQL_QUERY -> gqlQueryInitial(params)
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

    private suspend fun helixInitial(params: LoadInitialParams): List<Clip> {
        api = C.HELIX
        val get = helixApi.getClips(helixClientId, helixToken, channelId, null, started_at, ended_at, params.requestedLoadSize, offset)
        val list = mutableListOf<Clip>()
        get.data?.let { list.addAll(it) }
        val userIds = mutableListOf<String>()
        val gameIds = mutableListOf<String>()
        for (i in list) {
            i.broadcaster_login = channelLogin
            i.game_id?.let { gameIds.add(it) }
        }
        if (userIds.isNotEmpty()) {
            val users = helixApi.getUserById(helixClientId, helixToken, userIds).data
            if (users != null) {
                for (i in users) {
                    val items = list.filter { it.broadcaster_id == i.id }
                    for (item in items) {
                        item.broadcaster_login = i.login
                        item.profileImageURL = i.profile_image_url
                    }
                }
            }
        }
        if (gameIds.isNotEmpty()) {
            val games = helixApi.getGames(helixClientId, helixToken, gameIds).data
            if (games != null) {
                for (i in games) {
                    val items = list.filter { it.game_id == i.id }
                    for (item in items) {
                        item.game_name = i.name
                    }
                }
            }
        }
        offset = get.pagination?.cursor
        return list
    }

    private suspend fun gqlQueryInitial(params: LoadInitialParams): List<Clip> {
        api = C.GQL_QUERY
        val get1 = apolloClient(XtraModule(), gqlClientId).query(UserClipsQuery(id = Optional.Present(channelId), sort = Optional.Present(gqlQueryPeriod), first = Optional.Present(params.requestedLoadSize), after = Optional.Present(offset))).execute().data?.user
        val get = get1?.clips?.edges
        val list = mutableListOf<Clip>()
        if (get != null) {
            for (i in get) {
                list.add(
                    Clip(
                        id = i?.node?.slug ?: "",
                        broadcaster_id = channelId,
                        broadcaster_login = get1.login,
                        broadcaster_name = get1.displayName,
                        video_id = i?.node?.video?.id,
                        videoOffsetSeconds = i?.node?.videoOffsetSeconds,
                        game_id = i?.node?.game?.id,
                        game_name = i?.node?.game?.displayName,
                        title = i?.node?.title,
                        view_count = i?.node?.viewCount,
                        created_at = i?.node?.createdAt,
                        duration = i?.node?.durationSeconds?.toDouble(),
                        thumbnail_url = i?.node?.thumbnailURL,
                        profileImageURL = get1.profileImageURL,
                    )
                )
            }
            offset = get.lastOrNull()?.cursor
            nextPage = get1.clips.pageInfo?.hasNextPage ?: true
        }
        return list
    }

    private suspend fun gqlInitial(params: LoadInitialParams): List<Clip> {
        api = C.GQL
        val get = gqlApi.loadChannelClips(gqlClientId, channelLogin, gqlPeriod, params.requestedLoadSize, offset)
        offset = get.cursor
        return get.data
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Clip>) {
        loadRange(params, callback) {
            when (api) {
                C.HELIX -> helixRange(params)
                C.GQL_QUERY -> gqlQueryRange(params)
                C.GQL -> gqlRange(params)
                else -> mutableListOf()
            }
        }
    }

    private suspend fun helixRange(params: LoadRangeParams): List<Clip> {
        val get = helixApi.getClips(helixClientId, helixToken, channelId, null, started_at, ended_at, params.loadSize, offset)
        val list = mutableListOf<Clip>()
        if (offset != null && offset != "") {
            get.data?.let { list.addAll(it) }
            val userIds = mutableListOf<String>()
            val gameIds = mutableListOf<String>()
            for (i in list) {
                i.broadcaster_login = channelLogin
                i.game_id?.let { gameIds.add(it) }
            }
            if (userIds.isNotEmpty()) {
                val users = helixApi.getUserById(helixClientId, helixToken, userIds).data
                if (users != null) {
                    for (i in users) {
                        val items = list.filter { it.broadcaster_id == i.id }
                        for (item in items) {
                            item.broadcaster_login = i.login
                            item.profileImageURL = i.profile_image_url
                        }
                    }
                }
            }
            if (gameIds.isNotEmpty()) {
                val games = helixApi.getGames(helixClientId, helixToken, gameIds).data
                if (games != null) {
                    for (i in games) {
                        val items = list.filter { it.game_id == i.id }
                        for (item in items) {
                            item.game_name = i.name
                        }
                    }
                }
            }
            offset = get.pagination?.cursor
        }
        return list
    }

    private suspend fun gqlQueryRange(params: LoadRangeParams): List<Clip> {
        val get1 = apolloClient(XtraModule(), gqlClientId).query(UserClipsQuery(id = Optional.Present(channelId), sort = Optional.Present(gqlQueryPeriod), first = Optional.Present(params.loadSize), after = Optional.Present(offset))).execute().data?.user
        val get = get1?.clips?.edges
        val list = mutableListOf<Clip>()
        if (get != null && nextPage && offset != null && offset != "") {
            for (i in get) {
                list.add(
                    Clip(
                        id = i?.node?.slug ?: "",
                        broadcaster_id = channelId,
                        broadcaster_login = get1.login,
                        broadcaster_name = get1.displayName,
                        video_id = i?.node?.video?.id,
                        videoOffsetSeconds = i?.node?.videoOffsetSeconds,
                        game_id = i?.node?.game?.id,
                        game_name = i?.node?.game?.displayName,
                        title = i?.node?.title,
                        view_count = i?.node?.viewCount,
                        created_at = i?.node?.createdAt,
                        duration = i?.node?.durationSeconds?.toDouble(),
                        thumbnail_url = i?.node?.thumbnailURL,
                        profileImageURL = get1.profileImageURL,
                    )
                )
            }
            offset = get.lastOrNull()?.cursor
            nextPage = get1.clips.pageInfo?.hasNextPage ?: true
        }
        return list
    }

    private suspend fun gqlRange(params: LoadRangeParams): List<Clip> {
        val get = gqlApi.loadChannelClips(gqlClientId, channelLogin, gqlPeriod, params.loadSize, offset)
        return if (offset != null && offset != "") {
            offset = get.cursor
            get.data
        } else mutableListOf()
    }

    class Factory(
        private val channelId: String?,
        private val channelLogin: String?,
        private val helixClientId: String?,
        private val helixToken: String?,
        private val started_at: String?,
        private val ended_at: String?,
        private val helixApi: HelixApi,
        private val gqlClientId: String?,
        private val gqlQueryPeriod: ClipsPeriod?,
        private val gqlPeriod: String?,
        private val gqlApi: GraphQLRepository,
        private val apiPref: ArrayList<Pair<Long?, String?>?>,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Clip, ChannelClipsDataSource>() {

        override fun create(): DataSource<Int, Clip> =
                ChannelClipsDataSource(channelId, channelLogin, helixClientId, helixToken, started_at, ended_at, helixApi, gqlClientId, gqlQueryPeriod, gqlPeriod, gqlApi, apiPref, coroutineScope).also(sourceLiveData::postValue)
    }
}
