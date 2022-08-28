package com.github.andreyasadchy.xtra.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.andreyasadchy.xtra.ui.chat.ChannelChatViewModel
import com.github.andreyasadchy.xtra.ui.chat.ChatViewModel
import com.github.andreyasadchy.xtra.ui.common.GenericViewModelFactory
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsViewModel
import com.github.andreyasadchy.xtra.ui.login.LoginViewModel
import com.github.andreyasadchy.xtra.ui.main.MainViewModel
import com.github.andreyasadchy.xtra.ui.search.channels.ChannelSearchViewModel
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsViewModel
import com.github.andreyasadchy.xtra.ui.view.chat.MessageClickedViewModel
import com.github.andreyasadchy.xtra.ui.view.chat.StreamInfoViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

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
