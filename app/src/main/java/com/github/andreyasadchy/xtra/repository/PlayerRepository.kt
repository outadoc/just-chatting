package com.github.andreyasadchy.xtra.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.github.andreyasadchy.xtra.api.GraphQLApi
import com.github.andreyasadchy.xtra.api.MiscApi
import com.github.andreyasadchy.xtra.api.TTVLolApi
import com.github.andreyasadchy.xtra.api.UsherApi
import com.github.andreyasadchy.xtra.db.RecentEmotesDao
import com.github.andreyasadchy.xtra.db.VideoPositionsDao
import com.github.andreyasadchy.xtra.model.VideoPosition
import com.github.andreyasadchy.xtra.model.chat.*
import com.github.andreyasadchy.xtra.model.gql.playlist.VideoPlaylistTokenResponse
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.set
import kotlin.random.Random

private const val TAG = "PlayerRepository"

@Singleton
class PlayerRepository @Inject constructor(
    private val usher: UsherApi,
    private val misc: MiscApi,
    private val graphQL: GraphQLApi,
    private val recentEmotes: RecentEmotesDao,
    private val videoPositions: VideoPositionsDao,
    private val ttvLolApi: TTVLolApi
) {

    suspend fun loadStreamPlaylistUrl(gqlclientId: String, channelName: String, playerType: String, useAdblock: Boolean, randomDeviceId: Boolean, xdeviceid: String, deviceid: String): Pair<Uri, Boolean> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting stream playlist for channel $channelName. Player type: $playerType")

        //removes "commercial break in progress"
//        val uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 16)
//        val apiToken = UUID.randomUUID().toString().replace("-", "").substring(0, 32)
//        val serverSessionId = UUID.randomUUID().toString().replace("-", "").substring(0, 32)
//        val cookie = "unique_id=$uniqueId; unique_id_durable=$uniqueId; twitch.lohp.countryCode=BY; api_token=twilight.$apiToken; server_session_id=$serverSessionId"
//        val accessToken = api.getStreamAccessToken(clientId, cookie, channelName, token, playerType)

        if (useAdblock && ttvLolApi.ping().let { it.isSuccessful && it.body()!!.string() != "0" }) {
            buildUrl(
                "https://api.ttv.lol/playlist/$channelName.m3u8%3F", //manually insert "?" everywhere, some problem with encoding, too lazy for a proper solution
                "allow_source", "true",
                "allow_audio_only", "true",
                "type", "any",
                "p", Random.nextInt(999999).toString(),
                "fast_bread", "true",
                "player_backend", "mediaplayer",
                "supported_codecs", "avc1",
                "player_version", "1.4.0",
                "warp", "true"
            ) to true
        } else {
            val accessTokenJson = getAccessTokenJson(isLive = true, isVod = false, login = channelName, playerType = playerType, vodId = "")
            val accessTokenHeaders = getAccessTokenHeaders(randomDeviceId, xdeviceid, deviceid)
            accessTokenHeaders["Authorization"] = ""
            val accessToken = graphQL.getStreamAccessToken(gqlclientId, accessTokenHeaders, accessTokenJson)
            buildUrl(
                "https://usher.ttvnw.net/api/channel/hls/$channelName.m3u8?",
                "allow_source", "true",
                "allow_audio_only", "true",
                "type", "any",
                "p", Random.nextInt(999999).toString(),
                "fast_bread", "true", //low latency
                "sig", accessToken.signature,
                "token", accessToken.token
            ) to false
        }
    }

    suspend fun loadVideoPlaylistUrl(gqlclientId: String, videoId: String): Uri = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting video playlist url for video $videoId")
        val accessToken = loadVideoPlaylistAccessToken(gqlclientId, videoId)
        buildUrl(
            "https://usher.ttvnw.net/vod/$videoId.m3u8?",
            "token", accessToken.token,
            "sig", accessToken.signature,
            "allow_source", "true",
            "allow_audio_only", "true",
            "type", "any",
            "p", Random.nextInt(999999).toString()
        )
    }

    suspend fun loadVideoPlaylist(gqlclientId: String, videoId: String): Response<ResponseBody> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting video playlist for video $videoId")
        val accessToken = loadVideoPlaylistAccessToken(gqlclientId, videoId)
        val playlistQueryOptions = HashMap<String, String>()
        playlistQueryOptions["token"] = accessToken.token
        playlistQueryOptions["sig"] = accessToken.signature
        playlistQueryOptions["allow_source"] = "true"
        playlistQueryOptions["allow_audio_only"] = "true"
        playlistQueryOptions["type"] = "any"
        playlistQueryOptions["p"] = Random.nextInt(999999).toString()
        usher.getVideoPlaylist(videoId, playlistQueryOptions)
    }

    suspend fun loadGlobalBadges(): Response<TwitchBadgesResponse> = withContext(Dispatchers.IO) {
        misc.getGlobalBadges()
    }

    suspend fun loadChannelBadges(channelId: String): Response<TwitchBadgesResponse> = withContext(Dispatchers.IO) {
        misc.getChannelBadges(channelId)
    }

    suspend fun loadGlobalStvEmotes(): Response<StvEmotesResponse> = withContext(Dispatchers.IO) {
        misc.getGlobalStvEmotes()
    }

    suspend fun loadGlobalBttvEmotes(): Response<BttvGlobalResponse> = withContext(Dispatchers.IO) {
        misc.getGlobalBttvEmotes()
    }

    suspend fun loadBttvGlobalFfzEmotes(): Response<BttvFfzResponse> = withContext(Dispatchers.IO) {
        misc.getBttvGlobalFfzEmotes()
    }

    suspend fun loadStvEmotes(channelId: String): Response<StvEmotesResponse> = withContext(Dispatchers.IO) {
        misc.getStvEmotes(channelId)
    }

    suspend fun loadBttvEmotes(channelId: String): Response<BttvChannelResponse> = withContext(Dispatchers.IO) {
        misc.getBttvEmotes(channelId)
    }

    suspend fun loadBttvFfzEmotes(channelId: String): Response<BttvFfzResponse> = withContext(Dispatchers.IO) {
        misc.getBttvFfzEmotes(channelId)
    }

    fun loadRecentEmotes() = recentEmotes.getAll()

    fun insertRecentEmotes(emotes: Collection<RecentEmote>) {
        GlobalScope.launch {
            val listSize = emotes.size
            val list = if (listSize <= RecentEmote.MAX_SIZE) {
                emotes
            } else {
                emotes.toList().subList(listSize - RecentEmote.MAX_SIZE, listSize)
            }
            recentEmotes.ensureMaxSizeAndInsert(list)
        }
    }

    fun loadVideoPositions(): LiveData<Map<Long, Long>> = Transformations.map(videoPositions.getAll()) { list ->
        list.associate { it.id to it.position }
    }

    fun saveVideoPosition(position: VideoPosition) {
        GlobalScope.launch {
            videoPositions.insert(position)
        }
    }

    private fun getAccessTokenJson(isLive: Boolean, isVod: Boolean, login: String, playerType: String, vodId: String): JsonArray {
        val jsonArray = JsonArray(1)
        val operation = JsonObject().apply {
            addProperty("operationName", "PlaybackAccessToken")
            add("variables", JsonObject().apply {
                addProperty("isLive", isLive)
                addProperty("isVod", isVod)
                addProperty("login", login)
                addProperty("playerType", playerType)
                addProperty("vodID", vodId)
            })
            add("extensions", JsonObject().apply {
                add("persistedQuery", JsonObject().apply {
                    addProperty("version", 1)
                    addProperty("sha256Hash", "0828119ded1c13477966434e15800ff57ddacf13ba1911c129dc2200705b0712")
                })
            })
        }
        jsonArray.add(operation)
        return jsonArray
    }

    private fun getAccessTokenHeaders(randomDeviceId: Boolean = true, xdeviceid: String = "", deviceid: String = ""): MutableMap<String, String> {
        return HashMap<String, String>().apply {
            if (randomDeviceId) {
                val randomid = UUID.randomUUID().toString().replace("-", "").substring(0, 32) //X-Device-Id or Device-ID removes "commercial break in progress" (length 16 or 32)
                put("X-Device-Id", randomid)
                put("Device-ID", randomid)
            } else {
                if (xdeviceid != "")
                    put("X-Device-Id", xdeviceid)
                if (deviceid != "")
                    put("Device-ID", deviceid)
            }
            put("Accept", "*/*")
            put("Accept-Encoding", "gzip, deflate, br")
            put("Accept-Language", "ru-RU")
            put("Connection", "keep-alive")
            put("Content-Type", "text/plain;charset=UTF-8")
            put("Host", "gql.twitch.tv")
            put("Origin", "https://www.twitch.tv")
            put("Referer", "https://www.twitch.tv/")
            put("sec-ch-ua", "\"Google Chrome\";v=\"87\", \" Not;A Brand\";v=\"99\", \"Chromium\";v=\"87\"")
            put("sec-ch-ua-mobile", "?0")
            put("Sec-Fetch-Dest", "empty")
            put("Sec-Fetch-Mode", "cors")
            put("Sec-Fetch-Site", "same-site")
            put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36")
        }
    }

    private suspend fun loadVideoPlaylistAccessToken(gqlclientId: String, videoId: String): VideoPlaylistTokenResponse {
        //        val accessToken = api.getVideoAccessToken(clientId, id, token)
        val accessTokenJson = getAccessTokenJson(isLive = false, isVod = true, login = "", playerType = "channel_home_live", vodId = videoId)
        val accessTokenHeaders = getAccessTokenHeaders()
        // accessTokenHeaders["Authorization"] = ""
        return graphQL.getVideoAccessToken(gqlclientId, accessTokenHeaders, accessTokenJson)
    }

    private fun buildUrl(url: String, vararg queryParams: String): Uri {
        val stringBuilder = StringBuilder(url)
        stringBuilder.append(queryParams[0])
            .append("=")
            .append(queryParams[1])
        for (i in 2 until queryParams.size step 2) {
            stringBuilder.append("&")
                .append(queryParams[i])
                .append("=")
                .append(queryParams[i + 1])
        }
        return stringBuilder.toString().toUri()
    }
}