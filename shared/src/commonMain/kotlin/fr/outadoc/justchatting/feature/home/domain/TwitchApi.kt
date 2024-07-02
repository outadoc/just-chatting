package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.home.domain.model.User
import kotlinx.coroutines.flow.Flow

internal interface TwitchApi {

    suspend fun getStreamsByUserId(ids: List<String>): Result<List<Stream>>

    suspend fun getStreamsByUserLogin(logins: List<String>): Result<List<Stream>>

    suspend fun getUsersById(ids: List<String>): Result<List<User>>

    suspend fun getUsersByLogin(logins: List<String>): Result<List<User>>

    suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>>

    suspend fun getCheerEmotes(userId: String?): Result<List<Emote>>

    suspend fun getGlobalBadges(): Result<List<TwitchBadge>>

    suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>>

    suspend fun getChannelSchedule(
        channelId: String,
        limit: Int,
        after: String?,
    ): Result<ChannelSchedule>

    suspend fun getFollowedChannels(userId: String): Flow<PagingData<ChannelFollow>>

    suspend fun getFollowedStreams(userId: String): Flow<PagingData<Stream>>

    suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>>
}
