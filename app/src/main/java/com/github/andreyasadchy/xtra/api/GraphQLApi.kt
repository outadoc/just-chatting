package com.github.andreyasadchy.xtra.api

import com.github.andreyasadchy.xtra.model.chat.EmoteCardResponse
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelViewerListDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowUserDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedChannelsDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedStreamsDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowingGameDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowingUserDataResponse
import com.github.andreyasadchy.xtra.model.gql.search.SearchChannelDataResponse
import com.github.andreyasadchy.xtra.model.gql.stream.ViewersDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.TagGameDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.TagGameStreamDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.TagSearchDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.TagSearchGameStreamDataResponse
import com.github.andreyasadchy.xtra.model.gql.tag.TagStreamDataResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@JvmSuppressWildcards
interface GraphQLApi {

    @POST(".")
    suspend fun getChannelViewerList(@Header("Client-ID") clientId: String?, @Body json: JsonObject): ChannelViewerListDataResponse

    @POST(".")
    suspend fun getSearchChannels(@Header("Client-ID") clientId: String?, @Body json: JsonObject): SearchChannelDataResponse

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
    suspend fun getViewerCount(@Header("Client-ID") clientId: String?, @Body json: JsonObject): ViewersDataResponse

    @POST(".")
    suspend fun getEmoteCard(@Header("Client-ID") clientId: String?, @Body json: JsonObject): EmoteCardResponse

    @POST(".")
    suspend fun getFollowedStreams(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): FollowedStreamsDataResponse

    @POST(".")
    suspend fun getFollowedChannels(@Header("Client-ID") clientId: String?, @Header("Authorization") token: String?, @Body json: JsonObject): FollowedChannelsDataResponse

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
}
