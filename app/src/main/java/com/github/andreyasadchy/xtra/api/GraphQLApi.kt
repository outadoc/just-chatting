package com.github.andreyasadchy.xtra.api

import com.github.andreyasadchy.xtra.model.gql.channel.ChannelClipsDataResponse
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelVideosDataResponse
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelViewerListDataResponse
import com.github.andreyasadchy.xtra.model.gql.clip.ClipDataResponse
import com.github.andreyasadchy.xtra.model.gql.clip.ClipUrlsResponse
import com.github.andreyasadchy.xtra.model.gql.clip.ClipVideoResponse
import com.github.andreyasadchy.xtra.model.gql.followed.*
import com.github.andreyasadchy.xtra.model.gql.game.GameClipsDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameStreamsDataResponse
import com.github.andreyasadchy.xtra.model.gql.game.GameVideosDataResponse
import com.github.andreyasadchy.xtra.model.gql.playlist.StreamPlaylistTokenResponse
import com.github.andreyasadchy.xtra.model.gql.playlist.VideoPlaylistTokenResponse
import com.github.andreyasadchy.xtra.model.gql.search.SearchChannelDataResponse
import com.github.andreyasadchy.xtra.model.gql.search.SearchGameDataResponse
import com.github.andreyasadchy.xtra.model.gql.stream.StreamDataResponse
import com.github.andreyasadchy.xtra.model.gql.stream.ViewersDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.*
import com.github.andreyasadchy.xtra.model.gql.vod.VodGamesDataResponse
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.POST

@JvmSuppressWildcards
interface GraphQLApi {

    @POST(".")
    suspend fun getStreamPlaybackAccessToken(@Header("Client-ID") clientId: String?, @HeaderMap headers: Map<String, String>, @Body json: JsonArray): StreamPlaylistTokenResponse

    @POST(".")
    suspend fun getVideoPlaybackAccessToken(@Header("Client-ID") clientId: String?, @HeaderMap headers: Map<String, String>, @Body json: JsonArray): VideoPlaylistTokenResponse

    @POST(".")
    suspend fun getClipUrls(@Header("Client-ID") clientId: String?, @Body json: JsonArray): ClipUrlsResponse

    @POST(".")
    suspend fun getClipData(@Header("Client-ID") clientId: String?, @Body json: JsonObject): ClipDataResponse

    @POST(".")
    suspend fun getClipVideo(@Header("Client-ID") clientId: String?, @Body json: JsonObject): ClipVideoResponse

    @POST(".")
    suspend fun getTopGames(@Header("Client-ID") clientId: String?, @Body json: JsonObject): GameDataResponse

    @POST(".")
    suspend fun getTopStreams(@Header("Client-ID") clientId: String?, @Body json: JsonObject): StreamDataResponse

    @POST(".")
    suspend fun getGameStreams(@Header("Client-ID") clientId: String?, @Body json: JsonObject): GameStreamsDataResponse

    @POST(".")
    suspend fun getGameVideos(@Header("Client-ID") clientId: String?, @Body json: JsonObject): GameVideosDataResponse

    @POST(".")
    suspend fun getGameClips(@Header("Client-ID") clientId: String?, @Body json: JsonObject): GameClipsDataResponse

    @POST(".")
    suspend fun getChannelVideos(@Header("Client-ID") clientId: String?, @Body json: JsonObject): ChannelVideosDataResponse

    @POST(".")
    suspend fun getChannelClips(@Header("Client-ID") clientId: String?, @Body json: JsonObject): ChannelClipsDataResponse

    @POST(".")
    suspend fun getChannelViewerList(@Header("Client-ID") clientId: String?, @Body json: JsonObject): ChannelViewerListDataResponse

    @POST(".")
    suspend fun getSearchChannels(@Header("Client-ID") clientId: String?, @Body json: JsonObject): SearchChannelDataResponse

    @POST(".")
    suspend fun getSearchGames(@Header("Client-ID") clientId: String?, @Body json: JsonObject): SearchGameDataResponse

    @POST(".")
    suspend fun getGameTags(@Header("Client-ID") clientId: String?, @Body json: JsonObject): TagGameDataResponse

    @POST(".")
    suspend fun getGameStreamTags(@Header("Client-ID") clientId: String?, @Body json: JsonObject): TagGameStreamDataResponse

    @POST(".")
    suspend fun getStreamTags(@Header("Client-ID") clientId: String?, @Body json: JsonObject): TagStreamDataResponse

    @POST(".")
    suspend fun getSearchGameTags(@Header("Client-ID") clientId: String?, @Body json: JsonObject): TagSearchGameStreamDataResponse

    @POST(".")
    suspend fun getSearchStreamTags(@Header("Client-ID") clientId: String?, @Body json: JsonObject): TagSearchDataResponse

    @POST(".")
    suspend fun getVodGames(@Header("Client-ID") clientId: String?, @Body json: JsonObject): VodGamesDataResponse

    @POST(".")
    suspend fun getViewerCount(@Header("Client-ID") clientId: String?, @Body json: JsonObject): ViewersDataResponse

    @POST(".")
    suspend fun getFollowedStreams(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): FollowedStreamsDataResponse

    @POST(".")
    suspend fun getFollowedVideos(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): FollowedVideosDataResponse

    @POST(".")
    suspend fun getFollowedChannels(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): FollowedChannelsDataResponse

    @POST(".")
    suspend fun getFollowedGames(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): FollowedGamesDataResponse

    @POST(".")
    suspend fun getFollowUser(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): FollowUserDataResponse

    @POST(".")
    suspend fun getUnfollowUser(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): JsonElement

    @POST(".")
    suspend fun getFollowGame(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): JsonElement

    @POST(".")
    suspend fun getUnfollowGame(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): JsonElement

    @POST(".")
    suspend fun getFollowingUser(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): FollowingUserDataResponse

    @POST(".")
    suspend fun getFollowingGame(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): FollowingGameDataResponse

    @POST(".")
    suspend fun getChannelPanel(@Body json: JsonArray): Response<ResponseBody>
}