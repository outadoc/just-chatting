package fr.outadoc.justchatting.utils.presentation

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

internal fun Context.toast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}

internal fun Context.shortToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}
