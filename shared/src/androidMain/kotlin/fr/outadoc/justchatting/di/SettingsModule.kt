package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.feature.preferences.data.SharedPrefsPreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.presentation.AndroidLogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.DefaultReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.LogRepository
import fr.outadoc.justchatting.feature.preferences.presentation.ReadExternalDependenciesList
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

public val settingsModule: Module = module {
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
    single<LogRepository> { AndroidLogRepository(get()) }
    single<ReadExternalDependenciesList> { DefaultReadExternalDependenciesList(get()) }
    single<PreferenceRepository> { SharedPrefsPreferenceRepository(get()) }
}
