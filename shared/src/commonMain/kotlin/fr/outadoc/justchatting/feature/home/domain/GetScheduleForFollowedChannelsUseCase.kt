package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import androidx.paging.map
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

internal class GetScheduleForFollowedChannelsUseCase(
    private val twitchRepository: TwitchRepository,
) {
    suspend operator fun invoke(
        currentTime: Instant,
        timeZone: TimeZone,
    ): Flow<PagingData<ChannelSchedule>> {
        return twitchRepository
            .getFollowedChannels()
            .map { channels ->
                channels.map { channel ->
                    ChannelSchedule(
                        user = channel.user,
                        scheduleFlow = twitchRepository.getChannelSchedule(
                            channelId = channel.user.id,
                            currentTime = currentTime,
                            timeZone = timeZone,
                        ),
                    )
                }
            }
    }
}
