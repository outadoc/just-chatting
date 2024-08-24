package fr.outadoc.justchatting.feature.widget

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.text.Text
import fr.outadoc.justchatting.feature.timeline.presentation.EpgViewModel
import fr.outadoc.justchatting.shared.R
import org.koin.compose.koinInject

internal class LiveWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val viewModel: EpgViewModel = koinInject()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.load()
            }

            GlanceTheme(colors = GlanceTheme.colors) {
                Scaffold(
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.ic_notif),
                            title = LocalContext.current.getString(R.string.epg_title),
                            actions = {
                                CircleIconButton(
                                    modifier = GlanceModifier.padding(8.dp),
                                    imageProvider = ImageProvider(R.drawable.ic_sync),
                                    contentDescription = LocalContext.current.getString(R.string.epg_refresh_action_cd),
                                    backgroundColor = null,
                                    key = "refresh",
                                    onClick = viewModel::load
                                )
                            }
                        )
                    }
                ) {
                    LazyColumn {
                        items(state.schedule.live) { userStream ->
                            Column {
                                Text(text = userStream.stream.title)
                                Text(text = userStream.user.displayName)
                            }
                        }
                    }
                }
            }
        }
    }
}
