package fr.outadoc.justchatting.component.twitch.http.api

import fr.outadoc.justchatting.component.twitch.http.model.ChannelSearchResponse
import fr.outadoc.justchatting.component.twitch.http.model.CheerEmotesResponse
import fr.outadoc.justchatting.component.twitch.http.model.EmoteSetResponse
import fr.outadoc.justchatting.component.twitch.http.model.FollowResponse
import fr.outadoc.justchatting.component.twitch.http.model.StreamsResponse
import fr.outadoc.justchatting.component.twitch.http.model.UsersResponse

interface HelixApi {

    suspend fun getStreams(ids: List<String>): StreamsResponse

    suspend fun getFollowedStreams(
        userId: String?,
        limit: Int,
        after: String? = null,
    ): StreamsResponse

    suspend fun getUsersById(ids: List<String>): UsersResponse

    suspend fun getUsersByLogin(logins: List<String>): UsersResponse

    suspend fun searchChannels(query: String, limit: Int, after: String?): ChannelSearchResponse

    suspend fun getFollowedChannels(
        userId: String?,
        limit: Int,
        after: String? = null,
    ): FollowResponse

    suspend fun getEmotesFromSet(setIds: List<String>): EmoteSetResponse

    suspend fun getCheerEmotes(userId: String?): CheerEmotesResponse
}
