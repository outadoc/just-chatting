package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.feature.home.presentation.MainRouterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainNavigationModule = module {
    viewModel { MainRouterViewModel(get(), get(), get(), get()) }
}
