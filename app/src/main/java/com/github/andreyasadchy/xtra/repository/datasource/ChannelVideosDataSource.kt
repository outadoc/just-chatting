package com.github.andreyasadchy.xtra.repository.datasource

import androidx.core.util.Pair
import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.UserVideosQuery
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.type.VideoSort
import com.github.andreyasadchy.xtra.util.C
import kotlinx.coroutines.CoroutineScope

class ChannelVideosDataSource (
    private val channelId: String?,
    private val channelLogin: String?,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixPeriod: Period,
    private val helixBroadcastTypes: BroadcastType,
    private val helixSort: Sort,
    private val helixApi: HelixApi,
    private val gqlClientId: String?,
    private val gqlQueryType: com.github.andreyasadchy.xtra.type.BroadcastType?,
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
                    C.GQL_QUERY -> if (helixPeriod == Period.ALL) gqlQueryInitial(params) else throw Exception()
                    C.GQL -> if (helixPeriod == Period.ALL) gqlInitial(params) else throw Exception()
                    else -> mutableListOf()
                }
            } catch (e: Exception) {
                try {
                    when (apiPref.elementAt(1)?.second) {
                        C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                        C.GQL_QUERY -> if (helixPeriod == Period.ALL) gqlQueryInitial(params) else throw Exception()
                        C.GQL -> if (helixPeriod == Period.ALL) gqlInitial(params) else throw Exception()
                        else -> mutableListOf()
                    }
                } catch (e: Exception) {
                    try {
                        when (apiPref.elementAt(2)?.second) {
                            C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                            C.GQL_QUERY -> if (helixPeriod == Period.ALL) gqlQueryInitial(params) else throw Exception()
                            C.GQL -> if (helixPeriod == Period.ALL) gqlInitial(params) else throw Exception()
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
        val get = helixApi.getChannelVideos(helixClientId, helixToken, channelId, helixPeriod, helixBroadcastTypes, helixSort, params.requestedLoadSize, offset)
        return if (get.data != null) {
            offset = get.pagination?.cursor
            get.data
        } else mutableListOf()
    }

    private suspend fun gqlQueryInitial(params: LoadInitialParams): List<Video> {
        api = C.GQL_QUERY
        val get1 = XtraModule_ApolloClientFactory.apolloClient(XtraModule(), gqlClientId)
            .query(UserVideosQuery(id = Optional.Present(channelId), sort = Optional.Present(gqlQuerySort), type = Optional.Present(gqlQueryType), first = Optional.Present(params.requestedLoadSize), after = Optional.Present(offset))).execute().data?.user
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
        return list
    }

    private suspend fun gqlInitial(params: LoadInitialParams): List<Video> {
        api = C.GQL
        val get = gqlApi.loadChannelVideos(gqlClientId, channelLogin, gqlType, gqlSort, params.requestedLoadSize, offset)
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
        val get = helixApi.getChannelVideos(helixClientId, helixToken, channelId, helixPeriod, helixBroadcastTypes, helixSort, params.loadSize, offset)
        return if (offset != null && offset != "") {
            if (get.data != null) {
                offset = get.pagination?.cursor
                get.data
            } else mutableListOf()
        } else mutableListOf()
    }

    private suspend fun gqlQueryRange(params: LoadRangeParams): List<Video> {
        val get1 = XtraModule_ApolloClientFactory.apolloClient(XtraModule(), gqlClientId).query(UserVideosQuery(id = Optional.Present(channelId), sort = Optional.Present(gqlQuerySort), type = Optional.Present(gqlQueryType), first = Optional.Present(params.loadSize), after = Optional.Present(offset))).execute().data?.user
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
        return list
    }

    private suspend fun gqlRange(params: LoadRangeParams): List<Video> {
        val get = gqlApi.loadChannelVideos(gqlClientId, channelLogin, gqlType, gqlSort, params.loadSize, offset)
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
        private val helixPeriod: Period,
        private val helixBroadcastTypes: BroadcastType,
        private val helixSort: Sort,
        private val helixApi: HelixApi,
        private val gqlClientId: String?,
        private val gqlQueryType: com.github.andreyasadchy.xtra.type.BroadcastType?,
        private val gqlQuerySort: VideoSort?,
        private val gqlType: String?,
        private val gqlSort: String?,
        private val gqlApi: GraphQLRepository,
        private val apiPref: ArrayList<Pair<Long?, String?>?>,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Video, ChannelVideosDataSource>() {

        override fun create(): DataSource<Int, Video> =
                ChannelVideosDataSource(channelId, channelLogin, helixClientId, helixToken, helixPeriod, helixBroadcastTypes, helixSort, helixApi, gqlClientId, gqlQueryType, gqlQuerySort, gqlType, gqlSort, gqlApi, apiPref, coroutineScope).also(sourceLiveData::postValue)
    }
}
