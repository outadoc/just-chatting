package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.feature.followed.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.recent.presentation.RecentChannelsViewModel
import fr.outadoc.justchatting.feature.search.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.feature.timeline.presentation.TimelineViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

public val homeModule: Module = module {
    viewModel { ChannelSearchViewModel(get()) }
    viewModel { FollowedChannelsViewModel(get()) }
    viewModel { RecentChannelsViewModel(get()) }
    viewModel { TimelineViewModel(get(), get()) }
}
