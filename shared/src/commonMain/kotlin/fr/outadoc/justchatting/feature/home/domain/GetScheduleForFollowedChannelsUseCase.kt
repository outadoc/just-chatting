package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import androidx.paging.map
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GetScheduleForFollowedChannelsUseCase(
    private val twitchRepository: TwitchRepository,
) {
    suspend operator fun invoke(): Flow<PagingData<ChannelSchedule>> {
        return twitchRepository
            .getFollowedChannels()
            .map { channels ->
                channels.map { channel ->
                    ChannelSchedule(
                        user = channel.user,
                        segments = twitchRepository.getChannelSchedule(
                            channelId = channel.user.id,
                        ),
                    )
                }
            }
    }
}
