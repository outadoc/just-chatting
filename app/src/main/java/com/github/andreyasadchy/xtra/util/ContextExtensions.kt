package com.github.andreyasadchy.xtra.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager

val Context.isNetworkAvailable get() = getConnectivityManager(this).activeNetworkInfo?.isConnectedOrConnecting == true

private fun getConnectivityManager(context: Context) =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

fun Context.prefs(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

fun Context.convertDpToPixels(dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.resources.displayMetrics)
        .toInt()

val Context.isDarkMode: Boolean
    get() = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

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
