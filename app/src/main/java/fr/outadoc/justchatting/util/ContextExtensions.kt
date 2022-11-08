package fr.outadoc.justchatting.util

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fr.outadoc.justchatting")

fun Context.toast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}

fun Context.shortToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

val Context.isNetworkAvailable: Boolean
    get() {
        val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = manager.activeNetworkInfo
        var connected = activeNetworkInfo != null && activeNetworkInfo.isConnected
        if (!connected) {
            connected = manager.allNetworkInfo.any { it.isConnected }
        }
        return connected
    }
