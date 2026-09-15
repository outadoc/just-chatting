package fr.outadoc.justchatting.feature.recent.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
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
import androidx.glance.appwidget.PreviewSizeMode
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
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.glance.GlanceUserItem
import fr.outadoc.justchatting.feature.shared.presentation.glance.WidgetPreviewData
import fr.outadoc.justchatting.feature.shared.presentation.glance.adaptiveGridCellsCompat
import fr.outadoc.justchatting.feature.shared.presentation.glance.rememberProfileImageProvider
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainActivity
import fr.outadoc.justchatting.shared.ui.R
import kotlinx.collections.immutable.ImmutableList
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

    // The picker shows a single image, so compose it at the size the widget asks to be placed at
    // (4x2 cells, per widget_recent.xml) rather than at its minimum, which fits one channel.
    override val previewSizeMode: PreviewSizeMode =
        SizeMode.Responsive(setOf(HORIZONTAL_RECTANGLE))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            val viewModel: RecentChannelsViewModel = koinInject()

            LaunchedEffect(Unit) {
                viewModel.refresh()
            }

            val state by viewModel.state.collectAsState()

            Scaffolded {
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
                        Channels(users = currentState.data)
                    }
                }
            }
        }
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        provideContent {
            Scaffolded {
                Channels(
                    users = WidgetPreviewData.users(LocalContext.current),
                    profileImage = { user -> WidgetPreviewData.profileImage(user) },
                )
            }
        }
    }

    @Composable
    private fun Scaffolded(content: @Composable () -> Unit) {
        GlanceTheme(colors = GlanceTheme.colors) {
            Scaffold(
                titleBar = {
                    TitleBar(
                        startIcon = ImageProvider(R.drawable.ic_notif),
                        title = LocalContext.current.getString(R.string.widget_recent_title),
                    )
                },
            ) {
                content()
            }
        }
    }

    @Composable
    private fun Channels(
        users: ImmutableList<User>,
        profileImage: @Composable (User) -> ImageProvider = { user -> rememberProfileImageProvider(user) },
    ) {
        LazyVerticalGrid(
            gridCells = adaptiveGridCellsCompat(minSize = 64.dp),
        ) {
            items(users) { user ->
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
                            profileImage = profileImage(user),
                        )
                    }
                }
            }
        }
    }
}
