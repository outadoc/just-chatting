package fr.outadoc.justchatting.component.twitch.domain.api

import androidx.paging.Pager
import fr.outadoc.justchatting.component.twitch.model.ChannelSearch
import fr.outadoc.justchatting.component.twitch.model.ChannelSearchResponse
import fr.outadoc.justchatting.component.twitch.model.CheerEmote
import fr.outadoc.justchatting.component.twitch.model.Follow
import fr.outadoc.justchatting.component.twitch.model.FollowResponse
import fr.outadoc.justchatting.component.twitch.model.Stream
import fr.outadoc.justchatting.component.twitch.model.StreamsResponse
import fr.outadoc.justchatting.component.twitch.model.TwitchEmote
import fr.outadoc.justchatting.component.twitch.model.User

interface TwitchRepository {

    suspend fun loadSearchChannels(
        query: String
    ): Pager<String, ChannelSearchResponse>

    suspend fun loadFollowedStreams(): Pager<String, StreamsResponse>
    suspend fun loadFollowedChannels(): Pager<String, FollowResponse>

    suspend fun mapFollowsWithUserProfileImages(follows: Collection<Follow>): Collection<Follow>
    suspend fun loadStreamWithUser(channelId: String): Stream?
    suspend fun loadUsersById(ids: List<String>): List<User>?
    suspend fun loadUsersByLogin(logins: List<String>): List<User>?
    suspend fun loadCheerEmotes(userId: String): List<CheerEmote>
    suspend fun loadEmotesFromSet(setIds: List<String>): List<TwitchEmote>?
    suspend fun loadUserFollowing(userId: String?, channelId: String?, userLogin: String?): Boolean
    suspend fun mapStreamsWithUserProfileImages(streams: Collection<Stream>): List<Stream>
    suspend fun mapSearchWithUserProfileImages(searchResults: List<ChannelSearch>): List<ChannelSearch>
}
