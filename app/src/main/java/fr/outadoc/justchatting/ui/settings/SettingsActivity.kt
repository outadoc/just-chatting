package fr.outadoc.justchatting.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.ui.main.BaseActivity

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Mdc3Theme {
                SettingsScreen(
                    onBackPress = ::finish,
                    onOpenNotificationPreferences = {
                        openSettingsIntent(action = Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    },
                    onOpenBubblePreferences = {
                        openSettingsIntent(action = Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS)
                    }
                )
            }
        }
    }

    private fun openSettingsIntent(action: String) {
        val intent = Intent().apply {
            this.action = action
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            // for Android 5-7
            putExtra("app_package", packageName)
            putExtra("app_uid", applicationInfo.uid)

            // for Android 8 and above
            putExtra("android.provider.extra.APP_PACKAGE", packageName)
        }

        startActivity(intent)
    }
}
