package fr.outadoc.justchatting.feature.recent.presentation.widget

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import fr.outadoc.justchatting.feature.recent.presentation.RecentChannelsViewModel
import fr.outadoc.justchatting.feature.shared.presentation.glance.GlanceUserItem
import fr.outadoc.justchatting.feature.shared.presentation.glance.adaptiveGridCellsCompat
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainActivity
import fr.outadoc.justchatting.shared.R
import org.koin.compose.koinInject

internal class RecentChannelsWidget : GlanceAppWidget() {
    companion object {
        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(250.dp, 100.dp)
        private val BIG_SQUARE = DpSize(250.dp, 250.dp)
    }

    override val sizeMode =
        SizeMode.Responsive(
            setOf(
                SMALL_SQUARE,
                HORIZONTAL_RECTANGLE,
                BIG_SQUARE,
            ),
        )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            val viewModel: RecentChannelsViewModel = koinInject()

            LaunchedEffect(Unit) {
                viewModel.refresh()
            }

            GlanceTheme(colors = GlanceTheme.colors) {
                Scaffold(
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.ic_notif),
                            title = LocalContext.current.getString(R.string.widget_recent_title),
                        )
                    },
                ) {
                    val state by viewModel.state.collectAsState()
                    when (val currentState = state) {
                        RecentChannelsViewModel.State.Loading -> {
                            Column(
                                modifier = GlanceModifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is RecentChannelsViewModel.State.Content -> {
                            LazyVerticalGrid(
                                gridCells = adaptiveGridCellsCompat(minSize = 64.dp),
                            ) {
                                items(currentState.data) { user ->
                                    Column {
                                        Box(
                                            modifier =
                                                GlanceModifier
                                                    .clickable(
                                                        MainActivity.createGlanceAction(
                                                            userId = user.id,
                                                        ),
                                                    ),
                                        ) {
                                            GlanceUserItem(
                                                modifier =
                                                    GlanceModifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),
                                                user = user,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
