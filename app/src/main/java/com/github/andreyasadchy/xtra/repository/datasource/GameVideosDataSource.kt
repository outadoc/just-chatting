package com.github.andreyasadchy.xtra.repository.datasource

import androidx.core.util.Pair
import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.GameVideosQuery
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.type.BroadcastType
import com.github.andreyasadchy.xtra.type.VideoSort
import com.github.andreyasadchy.xtra.util.C
import kotlinx.coroutines.CoroutineScope

class GameVideosDataSource private constructor(
    private val gameId: String?,
    private val gameName: String?,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixPeriod: Period,
    private val helixBroadcastTypes: com.github.andreyasadchy.xtra.model.helix.video.BroadcastType,
    private val helixLanguage: String?,
    private val helixSort: Sort,
    private val helixApi: HelixApi,
    private val gqlClientId: String?,
    private val gqlQueryLanguages: List<String>?,
    private val gqlQueryType: BroadcastType?,
    private val gqlQuerySort: VideoSort?,
    private val gqlType: String?,
    private val gqlSort: String?,
    private val gqlApi: GraphQLRepository,
    private val apiPref: ArrayList<Pair<Long?, String?>?>,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Video>(coroutineScope) {
    private var api: String? = null
    private var offset: String? = null
    private var nextPage: Boolean = true

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadInitial(params, callback) {
            try {
                when (apiPref.elementAt(0)?.second) {
                    C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                    C.GQL_QUERY -> if (helixPeriod == Period.WEEK) gqlQueryInitial(params) else throw Exception()
                    C.GQL -> if (helixLanguage.isNullOrBlank() && gqlQueryLanguages.isNullOrEmpty() && helixPeriod == Period.WEEK) gqlInitial(params) else throw Exception()
                    else -> mutableListOf()
                }
            } catch (e: Exception) {
                try {
                    when (apiPref.elementAt(1)?.second) {
                        C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                        C.GQL_QUERY -> if (helixPeriod == Period.WEEK) gqlQueryInitial(params) else throw Exception()
                        C.GQL -> if (helixLanguage.isNullOrBlank() && gqlQueryLanguages.isNullOrEmpty() && helixPeriod == Period.WEEK) gqlInitial(params) else throw Exception()
                        else -> mutableListOf()
                    }
                } catch (e: Exception) {
                    try {
                        when (apiPref.elementAt(2)?.second) {
                            C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                            C.GQL_QUERY -> if (helixPeriod == Period.WEEK) gqlQueryInitial(params) else throw Exception()
                            C.GQL -> if (helixLanguage.isNullOrBlank() && gqlQueryLanguages.isNullOrEmpty() && helixPeriod == Period.WEEK) gqlInitial(params) else throw Exception()
                            else -> mutableListOf()
                        }
                    } catch (e: Exception) {
                        mutableListOf()
                    }
                }
            }
        }
    }

    private suspend fun helixInitial(params: LoadInitialParams): List<Video> {
        api = C.HELIX
        val get = helixApi.getTopVideos(helixClientId, helixToken, gameId, helixPeriod, helixBroadcastTypes, helixLanguage, helixSort, params.requestedLoadSize, offset)
        val list = mutableListOf<Video>()
        get.data?.let { list.addAll(it) }
        val ids = mutableListOf<String>()
        for (i in list) {
            i.user_id?.let { ids.add(it) }
        }
        if (ids.isNotEmpty()) {
            val users = helixApi.getUserById(helixClientId, helixToken, ids).data
            if (users != null) {
                for (i in users) {
                    val items = list.filter { it.user_id == i.id }
                    for (item in items) {
                        item.profileImageURL = i.profile_image_url
                    }
                }
            }
        }
        offset = get.pagination?.cursor
        return list
    }

    private suspend fun gqlQueryInitial(params: LoadInitialParams): List<Video> {
        api = C.GQL_QUERY
        val typeList = if (gqlQueryType != null) mutableListOf(gqlQueryType) else null
        val get1 = apolloClient(XtraModule(), gqlClientId).query(GameVideosQuery(id = Optional.Present(gameId), languages = Optional.Present(gqlQueryLanguages), sort = Optional.Present(gqlQuerySort), type = Optional.Present(typeList), first = Optional.Present(params.requestedLoadSize), after = Optional.Present(offset))).execute().data?.game?.videos
        val get = get1?.edges
        val list = mutableListOf<Video>()
        if (get != null) {
            for (i in get) {
                list.add(
                    Video(
                        id = i?.node?.id ?: "",
                        user_id = i?.node?.owner?.id,
                        user_login = i?.node?.owner?.login,
                        user_name = i?.node?.owner?.displayName,
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
            nextPage = get1.pageInfo?.hasNextPage ?: true
        }
        return list
    }

    private suspend fun gqlInitial(params: LoadInitialParams): List<Video> {
        api = C.GQL
        val get = gqlApi.loadGameVideos(gqlClientId, gameName, gqlType, gqlSort, params.requestedLoadSize, offset)
        offset = get.cursor
        return get.data
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        loadRange(params, callback) {
            when (api) {
                C.HELIX -> helixRange(params)
                C.GQL_QUERY -> gqlQueryRange(params)
                C.GQL -> gqlRange(params)
                else -> mutableListOf()
            }
        }
    }

    private suspend fun helixRange(params: LoadRangeParams): List<Video> {
        val get = helixApi.getTopVideos(helixClientId, helixToken, gameId, helixPeriod, helixBroadcastTypes, helixLanguage, helixSort, params.loadSize, offset)
        val list = mutableListOf<Video>()
        if (offset != null && offset != "") {
            get.data?.let { list.addAll(it) }
            val ids = mutableListOf<String>()
            for (i in list) {
                i.user_id?.let { ids.add(it) }
            }
            if (ids.isNotEmpty()) {
                val users = helixApi.getUserById(helixClientId, helixToken, ids).data
                if (users != null) {
                    for (i in users) {
                        val items = list.filter { it.user_id == i.id }
                        for (item in items) {
                            item.profileImageURL = i.profile_image_url
                        }
                    }
                }
            }
            offset = get.pagination?.cursor
        }
        return list
    }

    private suspend fun gqlQueryRange(params: LoadRangeParams): List<Video> {
        val typeList = if (gqlQueryType != null) mutableListOf(gqlQueryType) else null
        val get1 = apolloClient(XtraModule(), gqlClientId).query(GameVideosQuery(id = Optional.Present(gameId), languages = Optional.Present(gqlQueryLanguages), sort = Optional.Present(gqlQuerySort), type = Optional.Present(typeList), first = Optional.Present(params.loadSize), after = Optional.Present(offset))).execute().data?.game?.videos
        val get = get1?.edges
        val list = mutableListOf<Video>()
        if (get != null && nextPage && offset != null && offset != "") {
            for (i in get) {
                list.add(
                    Video(
                        id = i?.node?.id ?: "",
                        user_id = i?.node?.owner?.id,
                        user_login = i?.node?.owner?.login,
                        user_name = i?.node?.owner?.displayName,
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
            nextPage = get1.pageInfo?.hasNextPage ?: true
        }
        return list
    }

    private suspend fun gqlRange(params: LoadRangeParams): List<Video> {
        val get = gqlApi.loadGameVideos(gqlClientId, gameName, gqlType, gqlSort, params.loadSize, offset)
        return if (offset != null && offset != "") {
            offset = get.cursor
            get.data
        } else mutableListOf()
    }

    class Factory (
        private val gameId: String?,
        private val gameName: String?,
        private val helixClientId: String?,
        private val helixToken: String?,
        private val helixPeriod: Period,
        private val helixBroadcastTypes: com.github.andreyasadchy.xtra.model.helix.video.BroadcastType,
        private val helixLanguage: String?,
        private val helixSort: Sort,
        private val helixApi: HelixApi,
        private val gqlClientId: String?,
        private val gqlQueryLanguages: List<String>?,
        private val gqlQueryType: BroadcastType?,
        private val gqlQuerySort: VideoSort?,
        private val gqlType: String?,
        private val gqlSort: String?,
        private val gqlApi: GraphQLRepository,
        private val apiPref: ArrayList<Pair<Long?, String?>?>,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Video, GameVideosDataSource>() {

        override fun create(): DataSource<Int, Video> =
                GameVideosDataSource(gameId, gameName, helixClientId, helixToken, helixPeriod, helixBroadcastTypes, helixLanguage, helixSort, helixApi, gqlClientId, gqlQueryLanguages, gqlQueryType, gqlQuerySort, gqlType, gqlSort, gqlApi, apiPref, coroutineScope).also(sourceLiveData::postValue)
    }
}
