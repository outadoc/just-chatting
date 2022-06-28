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

    fun loadSearchChannels(
        query: String,
        helixClientId: String?,
        helixToken: String?,
        coroutineScope: CoroutineScope
    ): Listing<ChannelSearch>

    fun loadFollowedStreams(
        userId: String?,
        helixClientId: String?,
        helixToken: String?,
        coroutineScope: CoroutineScope
    ): Listing<Stream>

    fun loadFollowedChannels(
        userId: String?,
        helixClientId: String?,
        helixToken: String?,
        sort: Sort,
        order: Order,
        coroutineScope: CoroutineScope
    ): Listing<Follow>

    suspend fun loadStreamWithUser(
        channelId: String,
        helixClientId: String?,
        helixToken: String?
    ): Stream?

    suspend fun loadUsersById(
        ids: List<String>,
        helixClientId: String?,
        helixToken: String?
    ): List<User>?

    suspend fun loadUsersByLogin(
        logins: List<String>,
        helixClientId: String?,
        helixToken: String?
    ): List<User>?

    suspend fun loadCheerEmotes(
        userId: String,
        helixClientId: String?,
        helixToken: String?
    ): List<CheerEmote>

    suspend fun loadEmotesFromSet(
        helixClientId: String?,
        helixToken: String?,
        setIds: List<String>
    ): List<TwitchEmote>?

    suspend fun loadUserFollowing(
        helixClientId: String?,
        helixToken: String?,
        userId: String?,
        channelId: String?,
        userLogin: String?
    ): Boolean
}
