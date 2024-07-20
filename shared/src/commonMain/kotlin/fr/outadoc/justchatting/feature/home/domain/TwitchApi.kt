package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleForDay
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.home.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

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
        currentTime: Instant,
        timeZone: TimeZone
    ): Flow<PagingData<ChannelScheduleForDay>>

    suspend fun getFollowedChannels(userId: String): Flow<PagingData<List<ChannelFollow>>>

    suspend fun getFollowedStreams(userId: String): Flow<PagingData<List<Stream>>>

    suspend fun searchChannels(query: String): Flow<PagingData<List<ChannelSearchResult>>>
}
