package fr.outadoc.justchatting.feature.followed.presentation.widget

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
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import fr.outadoc.justchatting.feature.followed.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.shared.presentation.glance.GlanceUserItem
import fr.outadoc.justchatting.feature.shared.presentation.glance.adaptiveGridCellsCompat
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainActivity
import fr.outadoc.justchatting.shared.R
import org.koin.compose.koinInject

internal class FollowedChannelsWidget : GlanceAppWidget() {
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
            val viewModel: FollowedChannelsViewModel = koinInject()

            LaunchedEffect(Unit) {
                viewModel.synchronize()
            }

            GlanceTheme(colors = GlanceTheme.colors) {
                Scaffold(
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.ic_notif),
                            title = LocalContext.current.getString(R.string.widget_channels_title),
                            actions = {
                                CircleIconButton(
                                    modifier = GlanceModifier.padding(8.dp),
                                    imageProvider = ImageProvider(R.drawable.ic_sync),
                                    contentDescription = LocalContext.current.getString(R.string.widget_refresh_action_cd),
                                    backgroundColor = null,
                                    key = "refresh",
                                    onClick = viewModel::synchronize,
                                )
                            },
                        )
                    },
                ) {
                    val state by viewModel.state.collectAsState()
                    LazyVerticalGrid(
                        gridCells = adaptiveGridCellsCompat(minSize = 64.dp),
                    ) {
                        items(state.data) { follow ->
                            Column {
                                Box(
                                    modifier =
                                    GlanceModifier
                                        .clickable(
                                            MainActivity.createGlanceAction(
                                                userId = follow.user.id,
                                            ),
                                        ),
                                ) {
                                    GlanceUserItem(
                                        modifier =
                                        GlanceModifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        user = follow.user,
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
