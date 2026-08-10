package fr.outadoc.justchatting.feature.shared.presentation

import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.followed.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.feature.search.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.feature.timeline.presentation.FutureTimelineViewModel
import fr.outadoc.justchatting.feature.timeline.presentation.LiveTimelineViewModel
import fr.outadoc.justchatting.utils.logging.LogStrategy
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

public class KoinHelper : KoinComponent {
    public fun getMainRouterViewModel(): MainRouterViewModel = get()
    public fun getChatViewModel(): ChatViewModel = get()
    public fun getFollowedChannelsViewModel(): FollowedChannelsViewModel = get()
    public fun getLiveTimelineViewModel(): LiveTimelineViewModel = get()
    public fun getFutureTimelineViewModel(): FutureTimelineViewModel = get()
    public fun getChannelSearchViewModel(): ChannelSearchViewModel = get()
    public fun getSettingsViewModel(): SettingsViewModel = get()
    public fun getDeeplinkReceiver(): DeeplinkReceiver = get()
    public fun getLogStrategy(): LogStrategy = get()
}
