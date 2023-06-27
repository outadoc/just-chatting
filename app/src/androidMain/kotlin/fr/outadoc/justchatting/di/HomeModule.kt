package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.feature.home.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.feature.home.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.home.presentation.FollowedStreamsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel { ChannelSearchViewModel(get()) }
    viewModel { FollowedChannelsViewModel(get()) }
    viewModel { FollowedStreamsViewModel(get()) }
}
