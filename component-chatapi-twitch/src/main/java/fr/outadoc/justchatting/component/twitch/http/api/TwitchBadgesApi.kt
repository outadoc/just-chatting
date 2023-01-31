package fr.outadoc.justchatting.component.twitch.http.api

import fr.outadoc.justchatting.component.twitch.http.model.TwitchBadgesResponse

interface TwitchBadgesApi {

    suspend fun getGlobalBadges(): TwitchBadgesResponse

    suspend fun getChannelBadges(channelId: String): TwitchBadgesResponse
}
