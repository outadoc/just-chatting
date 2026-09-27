package fr.outadoc.justchatting.feature.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.UserInfoViewModel
import fr.outadoc.justchatting.feature.followed.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.feature.search.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.feature.timeline.presentation.FutureTimelineViewModel
import fr.outadoc.justchatting.feature.timeline.presentation.LiveTimelineViewModel
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

/**
 * Creates the ViewModels of a SwiftUI view and owns them, like a `ViewModelStoreOwner` does on
 * the Compose side: requesting a ViewModel twice returns the same instance, and [clear] cancels
 * their `viewModelScope` once the view is gone.
 */
public class ViewModelOwner : KoinComponent {
    private val store = ViewModelStore()

    private val provider: ViewModelProvider by lazy {
        ViewModelProvider.create(
            store = store,
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(
                        modelClass: KClass<T>,
                        extras: CreationExtras,
                    ): T = getKoin().get(modelClass)
                },
        )
    }

    public val chatViewModel: ChatViewModel
        get() = provider[ChatViewModel::class]

    public val userInfoViewModel: UserInfoViewModel
        get() = provider[UserInfoViewModel::class]

    public val followedChannelsViewModel: FollowedChannelsViewModel
        get() = provider[FollowedChannelsViewModel::class]

    public val liveTimelineViewModel: LiveTimelineViewModel
        get() = provider[LiveTimelineViewModel::class]

    public val futureTimelineViewModel: FutureTimelineViewModel
        get() = provider[FutureTimelineViewModel::class]

    public val channelSearchViewModel: ChannelSearchViewModel
        get() = provider[ChannelSearchViewModel::class]

    public val settingsViewModel: SettingsViewModel
        get() = provider[SettingsViewModel::class]

    public fun clear() {
        store.clear()
    }
}
