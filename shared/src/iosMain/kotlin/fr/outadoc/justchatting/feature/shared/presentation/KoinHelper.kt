package fr.outadoc.justchatting.feature.shared.presentation

import fr.outadoc.justchatting.feature.followed.presentation.FollowedChannelsViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

public class KoinHelper : KoinComponent {
    public fun getMainRouterViewModel(): MainRouterViewModel = get()
    public fun getFollowedChannelsViewModel(): FollowedChannelsViewModel = get()
    public fun getDeeplinkReceiver(): DeeplinkReceiver = get()
}
