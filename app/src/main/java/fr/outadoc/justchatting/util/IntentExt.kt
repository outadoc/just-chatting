package fr.outadoc.justchatting.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

fun Intent.toPendingIntent(
    context: Context,
    mutable: Boolean
): PendingIntent {
    val mutableFlag =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && mutable) PendingIntent.FLAG_MUTABLE
        else 0

    val immutableFlag =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !mutable) PendingIntent.FLAG_IMMUTABLE
        else 0

    return PendingIntent.getActivity(
        context,
        0,
        this,
        PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag or immutableFlag
    )
}