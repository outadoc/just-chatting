package fr.outadoc.justchatting.repository

import androidx.paging.Pager
import fr.outadoc.justchatting.component.twitch.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.component.twitch.model.helix.channel.ChannelSearchResponse
import fr.outadoc.justchatting.component.twitch.model.helix.follows.Follow
import fr.outadoc.justchatting.component.twitch.model.helix.follows.FollowResponse
import fr.outadoc.justchatting.component.twitch.model.helix.stream.Stream
import fr.outadoc.justchatting.component.twitch.model.helix.stream.StreamsResponse
import fr.outadoc.justchatting.component.twitch.model.helix.user.User
import fr.outadoc.justchatting.component.twitch.model.chat.CheerEmote
import fr.outadoc.justchatting.component.twitch.model.chat.TwitchEmote

interface TwitchService {

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
