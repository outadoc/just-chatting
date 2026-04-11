package fr.outadoc.justchatting.feature.shared.domain

import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.Video
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

public interface TwitchApi {
    public suspend fun getStreamsByUserId(ids: List<String>): Result<List<Stream>>

    public suspend fun getStreamsByUserLogin(logins: List<String>): Result<List<Stream>>

    public suspend fun getUsersById(ids: List<String>): List<User>

    public suspend fun getUsersByLogin(logins: List<String>): List<User>

    public suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>>

    public suspend fun getCheerEmotes(userId: String?): Result<List<Emote>>

    public suspend fun getGlobalBadges(): Result<List<TwitchBadge>>

    public suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>>

    public suspend fun getChannelVideos(
        channelId: String,
        notBefore: Instant,
    ): Result<List<Video>>

    public suspend fun getChannelSchedule(
        userId: String,
        notBefore: Instant,
        notAfter: Instant,
    ): Result<List<ChannelScheduleSegment>>

    public suspend fun getFollowedChannels(userId: String): Result<List<ChannelFollow>>

    public suspend fun getFollowedStreams(userId: String): Result<List<Stream>>

    public suspend fun searchChannels(query: String): Flow<PagingData<List<ChannelSearchResult>>>

    public suspend fun sendChatMessage(
        channelUserId: String,
        senderUserId: String,
        message: String,
        inReplyToMessageId: String?,
    ): Result<String>
}
