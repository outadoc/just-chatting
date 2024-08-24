package fr.outadoc.justchatting.feature.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainActivity

internal class LiveWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(colors = GlanceTheme.colors) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Where to?",
                        modifier = GlanceModifier.padding(12.dp),
                    )

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
