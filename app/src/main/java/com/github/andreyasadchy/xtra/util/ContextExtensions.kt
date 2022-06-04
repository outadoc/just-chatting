package com.github.andreyasadchy.xtra.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Build
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.github.andreyasadchy.xtra.R
import java.util.Locale

val Context.isNetworkAvailable get() = getConnectivityManager(this).activeNetworkInfo?.isConnectedOrConnecting == true

private fun getConnectivityManager(context: Context) = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

fun Context.prefs(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

fun Context.convertDpToPixels(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.resources.displayMetrics).toInt()

fun Activity.applyTheme(): String {
    val lang = prefs().getString(C.UI_LANGUAGE, "auto") ?: "auto"
    if (lang != "auto") {
        val config = resources.configuration
        val locale = Locale(lang)
        Locale.setDefault(locale)
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createConfigurationContext(config)
        }
        resources.updateConfiguration(config, resources.displayMetrics)
        application.resources.updateConfiguration(config, resources.displayMetrics)
    }
    val theme = if (prefs().getBoolean(C.UI_THEME_FOLLOW_SYSTEM, false)) {
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> prefs().getString(C.UI_THEME_DARK_ON, "0")!!
            else -> prefs().getString(C.UI_THEME_DARK_OFF, "2")!!
        }
    } else {
        prefs().getString(C.THEME, "0")!!
    }
    setTheme(
        when (theme) {
            "1" -> R.style.AmoledTheme
            "2" -> R.style.LightTheme
            "3" -> R.style.BlueTheme
            else -> R.style.DarkTheme
        }
    )

    when (theme) {
        "1" -> window.statusBarColor = ContextCompat.getColor(this, R.color.primaryAmoled)
        "2" -> window.statusBarColor = ContextCompat.getColor(this, R.color.primaryLight)
        "3" -> window.statusBarColor = ContextCompat.getColor(this, R.color.primaryBlue)
        else -> window.statusBarColor = ContextCompat.getColor(this, R.color.primaryDark)
    }

    when (theme) {
        "1" -> window.navigationBarColor = ContextCompat.getColor(this, R.color.primaryAmoled)
        "2" -> window.navigationBarColor = ContextCompat.getColor(this, R.color.primaryLight)
        "3" -> window.navigationBarColor = ContextCompat.getColor(this, R.color.primaryBlue)
        else -> window.navigationBarColor = ContextCompat.getColor(this, R.color.primaryDark)
    }

    return theme
}

val Context.isActivityResumed
    get() = this !is Activity || !((isDestroyed) || isFinishing)

fun Context.toast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}

fun Context.shortToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.toast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Context.shortToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}
