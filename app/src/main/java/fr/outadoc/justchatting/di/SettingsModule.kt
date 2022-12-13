package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.preferences.domain.SharedPrefsPreferenceRepository
import fr.outadoc.justchatting.feature.settings.presentation.ReadExternalDependenciesList
import fr.outadoc.justchatting.feature.settings.presentation.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel { SettingsViewModel(get(), get()) }

    single { ReadExternalDependenciesList(get()) }
    single<PreferenceRepository> { SharedPrefsPreferenceRepository(get()) }
}
