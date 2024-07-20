package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.feature.home.domain.GetScheduleForFollowedChannelsUseCase
import fr.outadoc.justchatting.feature.home.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.feature.home.presentation.EpgViewModel
import fr.outadoc.justchatting.feature.home.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.home.presentation.FollowedStreamsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

public val homeModule: Module = module {
    viewModel { ChannelSearchViewModel(get()) }
    viewModel { FollowedChannelsViewModel(get()) }
    viewModel { FollowedStreamsViewModel(get()) }
    viewModel { EpgViewModel(get(), get()) }

    factory { GetScheduleForFollowedChannelsUseCase(get()) }
}
