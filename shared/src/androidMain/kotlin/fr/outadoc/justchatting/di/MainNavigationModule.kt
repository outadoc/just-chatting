package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.feature.home.presentation.MainRouterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

public val mainNavigationModule: Module = module {
    viewModel { MainRouterViewModel(get(), get(), get(), get()) }
}
