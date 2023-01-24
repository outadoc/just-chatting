package fr.outadoc.justchatting.component.twitch.api

import fr.outadoc.justchatting.component.twitch.model.TwitchBadgesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface TwitchBadgesApi {

    @GET("v1/badges/global/display")
    suspend fun getGlobalBadges(): TwitchBadgesResponse

    @GET("v1/badges/channels/{channelId}/display")
    suspend fun getChannelBadges(@Path("channelId") channelId: String): TwitchBadgesResponse
}
