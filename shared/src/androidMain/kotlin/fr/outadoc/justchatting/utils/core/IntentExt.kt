package fr.outadoc.justchatting.utils.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

private enum class IntentComponent {
    Activity, ForegroundService
}

fun Intent.toPendingActivityIntent(
    context: Context,
    mutable: Boolean = false,
): PendingIntent {
    return toPendingIntent(context, mutable, IntentComponent.Activity)
}

fun Intent.toPendingForegroundServiceIntent(
    context: Context,
    mutable: Boolean = false,
): PendingIntent {
    return toPendingIntent(context, mutable, IntentComponent.ForegroundService)
}

private fun Intent.toPendingIntent(
    context: Context,
    mutable: Boolean,
    intentComponent: IntentComponent,
): PendingIntent {
    val mutableFlag =
        if (Build.VERSION.SDK_INT >= 31 && mutable) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }

    val immutableFlag =
        if (Build.VERSION.SDK_INT >= 24 && !mutable) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

    val flags = PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag or immutableFlag

    return when (intentComponent) {
        IntentComponent.Activity -> PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ this,
            /* flags = */ flags,
        )

        IntentComponent.ForegroundService ->
            if (Build.VERSION.SDK_INT >= 26) {
                PendingIntent.getForegroundService(
                    /* context = */ context,
                    /* requestCode = */ 0,
                    /* intent = */ this,
                    /* flags = */ flags,
                )
            } else {
                PendingIntent.getService(
                    /* context = */ context,
                    /* requestCode = */ 0,
                    /* intent = */ this,
                    /* flags = */ flags,
                )
            }
    }
}
