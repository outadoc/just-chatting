package fr.outadoc.justchatting.feature.preferences.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fr.outadoc.justchatting")
