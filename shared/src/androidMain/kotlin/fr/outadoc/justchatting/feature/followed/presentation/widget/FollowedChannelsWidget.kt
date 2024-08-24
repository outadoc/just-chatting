package fr.outadoc.justchatting.feature.followed.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults
import fr.outadoc.justchatting.feature.chat.presentation.UserProfileImageContentProvider
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ChatActivity
import fr.outadoc.justchatting.feature.followed.presentation.FollowedChannelsViewModel
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.glance.GlanceCard
import fr.outadoc.justchatting.shared.R
import org.koin.compose.koinInject

internal class FollowedChannelsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val viewModel: FollowedChannelsViewModel = koinInject()

            LaunchedEffect(Unit) {
                viewModel.refresh()
            }

            GlanceTheme(colors = GlanceTheme.colors) {
                Scaffold(
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.ic_notif),
                            title = LocalContext.current.getString(R.string.channels),
                        )
                    },
                ) {
                    val state by viewModel.state.collectAsState()
                    when (val currentState = state) {
                        FollowedChannelsViewModel.State.Loading -> {
                            Column(
                                modifier = GlanceModifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is FollowedChannelsViewModel.State.Content -> {
                            LazyColumn {
                                items(currentState.data) { follow ->
                                    Column {
                                        GlanceCard(
                                            modifier = GlanceModifier
                                                .clickable(
                                                    ChatActivity.createGlanceAction(follow.user.id),
                                                ),
                                        ) {
                                            UserItem(
                                                modifier = GlanceModifier.fillMaxWidth(),
                                                user = follow.user,
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
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    modifier = GlanceModifier.size(32.dp),
                    provider = androidx.glance.appwidget.ImageProvider(
                        UserProfileImageContentProvider.createForUser(
                            context = LocalContext.current,
                            userId = user.id,
                        ),
                    ),
                    contentDescription = null,
                )

                Spacer(
                    modifier = GlanceModifier.width(8.dp),
                )

                Text(
                    text = user.displayName,
                    style = TextDefaults.defaultTextStyle.copy(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 16.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}
