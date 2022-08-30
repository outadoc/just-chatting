package fr.outadoc.justchatting.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fr.outadoc.justchatting.ui.chat.ChannelChatViewModel
import fr.outadoc.justchatting.ui.chat.ChatViewModel
import fr.outadoc.justchatting.ui.common.GenericViewModelFactory
import fr.outadoc.justchatting.ui.follow.channels.FollowedChannelsViewModel
import fr.outadoc.justchatting.ui.login.LoginViewModel
import fr.outadoc.justchatting.ui.main.MainViewModel
import fr.outadoc.justchatting.ui.search.channels.ChannelSearchViewModel
import fr.outadoc.justchatting.ui.streams.followed.FollowedStreamsViewModel
import fr.outadoc.justchatting.ui.view.chat.MessageClickedViewModel
import fr.outadoc.justchatting.ui.view.chat.StreamInfoViewModel

@Module
@Suppress("unused")
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: GenericViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowedStreamsViewModel::class)
    abstract fun bindFollowedStreamsViewModel(followedStreamsViewModel: FollowedStreamsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChannelSearchViewModel::class)
    abstract fun bindChannelSearchViewModel(channelSearchViewModel: ChannelSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChannelChatViewModel::class)
    abstract fun bindChannelPagerViewModel(channelChatViewModel: ChannelChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MessageClickedViewModel::class)
    abstract fun bindMessageClickedViewModel(messageClickedViewModel: MessageClickedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StreamInfoViewModel::class)
    abstract fun bindStreamInfoViewModel(streamInfoViewModel: StreamInfoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    abstract fun bindChatViewModel(chatViewModel: ChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowedChannelsViewModel::class)
    abstract fun bindFollowedChannelsViewModel(followedChannelsViewModel: FollowedChannelsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel
}
