package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.feature.home.presentation.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainNavigationModule = module {
    viewModel { MainViewModel(get(), get(), get(), get()) }
}
