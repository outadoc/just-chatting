package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import android.content.Context
import android.content.pm.PackageManager

internal class AndroidAppVersionNameProvider(
    private val context: Context,
) : AppVersionNameProvider {

    override val appVersionName: String?
        get() = try {
            context.applicationContext
                .packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
}
