package fr.outadoc.justchatting.feature.shared.presentation.glance

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.os.ConfigurationCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetManager.Companion.SET_WIDGET_PREVIEWS_RESULT_SUCCESS
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import fr.outadoc.justchatting.feature.followed.presentation.widget.FollowedChannelsWidgetReceiver
import fr.outadoc.justchatting.feature.recent.presentation.widget.RecentChannelsWidgetReceiver
import fr.outadoc.justchatting.feature.timeline.presentation.widget.LiveWidgetReceiver
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logInfo
import fr.outadoc.justchatting.utils.logging.logWarning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

private const val TAG = "WidgetPreviews"

private const val PREFERENCES_NAME = "widget_previews"
private const val KEY_PUBLISHED_SIGNATURE = "published_signature"

private val WIDGET_RECEIVERS: List<KClass<out GlanceAppWidgetReceiver>> =
    listOf(
        LiveWidgetReceiver::class,
        FollowedChannelsWidgetReceiver::class,
        RecentChannelsWidgetReceiver::class,
    )

/**
 * Renders each widget's `providePreview` composition and hands the result to the launcher, so that
 * the widget picker shows what a widget actually looks like rather than just the app icon.
 *
 * Only Android 15 and above can be given a generated preview. Everywhere else - and in the window
 * after an app update, which clears the generated previews - the picker falls back to the
 * `previewLayout` each widget's `appwidget-provider` XML declares.
 *
 * Replacing a preview is rate-limited to roughly two calls per provider per hour, so this publishes
 * only when it has to: when a provider has no generated preview, or when [publishedSignature] says
 * the one it has was rendered by a different version or in a different language.
 */
internal suspend fun publishWidgetPreviews(context: Context) {
    if (Build.VERSION.SDK_INT < 35) return

    val appWidgetManager: AppWidgetManager = context.getSystemService() ?: return
    val glanceAppWidgetManager = GlanceAppWidgetManager(context)

    val preferences: SharedPreferences =
        withContext(Dispatchers.IO) {
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        }

    val signature: String = publishedSignature(context)
    val isSignatureCurrent: Boolean = preferences.getString(KEY_PUBLISHED_SIGNATURE, null) == signature

    var publishedEverything = true

    for (receiver in WIDGET_RECEIVERS) {
        if (isSignatureCurrent && appWidgetManager.hasGeneratedPreview(context, receiver)) {
            logDebug(TAG) { "${receiver.simpleName} already has an up-to-date preview" }
            continue
        }

        try {
            val result = glanceAppWidgetManager.setWidgetPreviews(receiver)
            if (result == SET_WIDGET_PREVIEWS_RESULT_SUCCESS) {
                logInfo(TAG) { "Published the generated preview for ${receiver.simpleName}" }
            } else {
                logWarning(TAG) { "Rate-limited while setting the preview for ${receiver.simpleName}" }
                publishedEverything = false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logError(TAG, e) { "Failed to set the preview for ${receiver.simpleName}" }
            publishedEverything = false
        }
    }

    // Only remember the signature once every widget is covered, so that whatever was rate-limited
    // or failed is tried again on the next launch rather than written off.
    if (publishedEverything && !isSignatureCurrent) {
        withContext(Dispatchers.IO) {
            preferences.edit { putString(KEY_PUBLISHED_SIGNATURE, signature) }
        }
    }
}

/**
 * Identifies the previews the app would generate right now.
 *
 * A preview is a rendered image, so it goes stale whenever what it would look like changes: the
 * app version covers the widget layouts and the fixtures, which both ship in the APK, and the
 * locale covers the text they contain.
 *
 * An app update clears the generated previews anyway, so keying on the version is belt and braces;
 * the locale is the case that would otherwise leave a preview in the wrong language for good.
 */
private fun publishedSignature(context: Context): String {
    val versionCode: Long =
        try {
            PackageInfoCompat.getLongVersionCode(
                context.packageManager.getPackageInfo(context.packageName, 0),
            )
        } catch (e: Exception) {
            logError(TAG, e) { "Could not read the app's version code" }
            0L
        }

    val locales: String =
        ConfigurationCompat
            .getLocales(context.resources.configuration)
            .toLanguageTags()

    return "$versionCode/$locales"
}

@RequiresApi(35)
private fun AppWidgetManager.hasGeneratedPreview(
    context: Context,
    receiver: KClass<out GlanceAppWidgetReceiver>,
): Boolean {
    val component = ComponentName(context, receiver.java)
    val providerInfo: AppWidgetProviderInfo =
        installedProviders.firstOrNull { info -> info.provider == component }
            ?: return false

    return providerInfo.generatedPreviewCategories != 0
}
