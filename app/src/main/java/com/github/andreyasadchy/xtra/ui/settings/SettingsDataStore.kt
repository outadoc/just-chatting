package com.github.andreyasadchy.xtra.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceDataStore
import com.github.andreyasadchy.xtra.util.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class SettingsDataStore(applicationContext: Context) : PreferenceDataStore() {

    private val dataStore: DataStore<Preferences> = applicationContext.dataStore

    override fun getString(key: String, defValue: String?): String {
        return runBlocking {
            dataStore.data.map { prefs -> prefs[stringPreferencesKey(key)] ?: defValue!! }.first()
        }
    }
    override fun putString(key: String, value: String?) {
        runBlocking {
            dataStore.edit { prefs -> prefs[stringPreferencesKey(key)] = value!! }
        }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return runBlocking {
            dataStore.data.map { prefs -> prefs[booleanPreferencesKey(key)] ?: defValue }.first()
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        runBlocking {
            dataStore.edit { prefs -> prefs[booleanPreferencesKey(key)] = value }
        }
    }

    override fun getInt(key: String, defValue: Int): Int {
        return runBlocking {
            dataStore.data.map { prefs -> prefs[intPreferencesKey(key)] ?: defValue }.first()
        }
    }

    override fun putInt(key: String, value: Int) {
        runBlocking {
            dataStore.edit { prefs -> prefs[intPreferencesKey(key)] = value }
        }
    }
}
