package fr.outadoc.justchatting.feature.recent.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.ImageProvider
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults
import fr.outadoc.justchatting.feature.chat.presentation.UserProfileImageContentProvider
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ChatActivity
import fr.outadoc.justchatting.feature.recent.presentation.RecentChannelsViewModel
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.glance.adaptiveGridCellsCompat
import fr.outadoc.justchatting.shared.R
import org.koin.compose.koinInject

internal class RecentChannelsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
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
                            title = LocalContext.current.getString(R.string.chat_header_recent),
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
                                            modifier = GlanceModifier
                                                .clickable(
                                                    ChatActivity.createGlanceAction(user.id),
                                                ),
                                        ) {
                                            UserItem(
                                                modifier = GlanceModifier.fillMaxWidth(),
                                                user = user,
                                            )
                                        }

                                        Spacer(
                                            modifier = GlanceModifier.height(8.dp),
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

    @Composable
    private fun UserItem(
        modifier: GlanceModifier = GlanceModifier,
        user: User,
    ) {
        Column(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = GlanceModifier.size(48.dp),
                provider = ImageProvider(
                    UserProfileImageContentProvider.createForUser(
                        context = LocalContext.current,
                        userId = user.id,
                    ),
                ),
                contentDescription = null,
            )

            Spacer(
                modifier = GlanceModifier.height(8.dp),
            )

            Text(
                text = user.displayName,
                style = TextDefaults.defaultTextStyle.copy(
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
                maxLines = 1,
            )
        }
    }
}
