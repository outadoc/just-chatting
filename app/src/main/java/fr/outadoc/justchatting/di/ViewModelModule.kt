package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.ui.chat.ChatViewModel
import fr.outadoc.justchatting.ui.follow.channels.FollowedChannelsViewModel
import fr.outadoc.justchatting.ui.login.LoginViewModel
import fr.outadoc.justchatting.ui.main.MainViewModel
import fr.outadoc.justchatting.ui.search.channels.ChannelSearchViewModel
import fr.outadoc.justchatting.ui.streams.followed.FollowedStreamsViewModel
import fr.outadoc.justchatting.ui.view.chat.MessageClickedViewModel
import fr.outadoc.justchatting.ui.view.chat.StreamInfoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { ChannelSearchViewModel(get()) }
    viewModel { ChatViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { FollowedChannelsViewModel(get()) }
    viewModel { FollowedStreamsViewModel(get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel { MessageClickedViewModel(get()) }
    viewModel { StreamInfoViewModel(get()) }
}
