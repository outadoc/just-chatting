package fr.outadoc.justchatting.feature.widget

import android.content.Context
import androidx.compose.ui.res.stringResource
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainActivity
import fr.outadoc.justchatting.shared.R

internal class LiveWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(colors = GlanceTheme.colors) {
                Scaffold(
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.ic_notif),
                            title = LocalContext.current.getString(R.string.app_name),
                        )
                    }
                ) {
                    Row(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            text = "Home",
                            onClick = actionStartActivity<MainActivity>(),
                        )

                        Button(
                            text = "Work",
                            onClick = actionStartActivity<MainActivity>(),
                        )
                    }
                }
            }
        }
    }
}
