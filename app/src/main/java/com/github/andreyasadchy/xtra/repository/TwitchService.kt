package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearch
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.follows.Sort
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.user.User
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
