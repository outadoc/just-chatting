package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import android.content.Context
import android.content.pm.PackageManager

val Context.applicationVersionName: String?
    get() = try {
        applicationContext
            .packageManager
            .getPackageInfo(packageName, 0)
            .versionName
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
