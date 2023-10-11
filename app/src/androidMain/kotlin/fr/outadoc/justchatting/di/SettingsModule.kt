package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.preferences.domain.SharedPrefsPreferenceRepository
import fr.outadoc.justchatting.feature.preferences.presentation.AndroidLogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.DefaultReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.ReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel { SettingsViewModel(get(), get(), get()) }
    single { AndroidLogRepository(get()) }
    single<ReadExternalDependenciesList> { DefaultReadExternalDependenciesList(get()) }
    single<PreferenceRepository> { SharedPrefsPreferenceRepository(get()) }
}
