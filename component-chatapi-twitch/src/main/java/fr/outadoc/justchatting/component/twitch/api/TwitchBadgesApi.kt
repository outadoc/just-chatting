package fr.outadoc.justchatting.component.twitch.api

import fr.outadoc.justchatting.component.twitch.model.TwitchBadgesResponse

interface TwitchBadgesApi {

    suspend fun getGlobalBadges(): TwitchBadgesResponse

    suspend fun getChannelBadges(channelId: String): TwitchBadgesResponse
}
