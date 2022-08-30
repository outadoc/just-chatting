package fr.outadoc.justchatting.repository

import fr.outadoc.justchatting.model.chat.CheerEmote
import fr.outadoc.justchatting.model.chat.TwitchEmote
import fr.outadoc.justchatting.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.model.helix.follows.Order
import fr.outadoc.justchatting.model.helix.follows.Sort
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import kotlinx.coroutines.CoroutineScope

interface TwitchService {

    suspend fun loadSearchChannels(
        query: String,
        coroutineScope: CoroutineScope
    ): Listing<ChannelSearch>

    suspend fun loadFollowedStreams(coroutineScope: CoroutineScope): Listing<Stream>
    suspend fun loadFollowedChannels(
        sort: Sort,
        order: Order,
        coroutineScope: CoroutineScope
    ): Listing<Follow>

    suspend fun loadStreamWithUser(channelId: String): Stream?
    suspend fun loadUsersById(ids: List<String>): List<User>?
    suspend fun loadUsersByLogin(logins: List<String>): List<User>?
    suspend fun loadCheerEmotes(userId: String): List<CheerEmote>
    suspend fun loadEmotesFromSet(setIds: List<String>): List<TwitchEmote>?
    suspend fun loadUserFollowing(userId: String?, channelId: String?, userLogin: String?): Boolean
}
