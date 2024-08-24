package fr.outadoc.justchatting.feature.timeline.presentation.widget

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
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.ImageProvider
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults
import fr.outadoc.justchatting.feature.chat.presentation.UserProfileImageContentProvider
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ChatActivity
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.glance.GlanceCard
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
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
                            title = LocalContext.current.getString(R.string.live),
                            actions = {
                                CircleIconButton(
                                    modifier = GlanceModifier.padding(8.dp),
                                    imageProvider = ImageProvider(R.drawable.ic_sync),
                                    contentDescription = LocalContext.current.getString(R.string.epg_refresh_action_cd),
                                    backgroundColor = null,
                                    key = "refresh",
                                    onClick = viewModel::load,
                                )
                            },
                        )
                    },
                ) {
                    LazyColumn {
                        items(state.schedule.live) { userStream ->
                            GlanceCard(
                                modifier = GlanceModifier
                                    .padding(bottom = 8.dp)
                                    .clickable(
                                        ChatActivity.createGlanceAction(userStream.user.id),
                                    ),
                            ) {
                                LiveStream(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    user = userStream.user,
                                    stream = userStream.stream,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LiveStream(
        modifier: GlanceModifier = GlanceModifier,
        user: User,
        stream: Stream,
    ) {
        Column(
            modifier = modifier,
        ) {
            Text(
                text = stream.title,
                style = TextDefaults.defaultTextStyle.copy(
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
                maxLines = 2,
            )

            Spacer(
                modifier = GlanceModifier.height(4.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    modifier = GlanceModifier.size(20.dp),
                    provider = ImageProvider(
                        UserProfileImageContentProvider.createForUser(
                            context = LocalContext.current,
                            userId = user.id,
                        ),
                    ),
                    contentDescription = null,
                )

                Spacer(
                    modifier = GlanceModifier.width(4.dp),
                )

                Text(
                    text = user.displayName,
                    style = TextDefaults.defaultTextStyle.copy(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                if (stream.category != null) {
                    Text(
                        text = " • ",
                        style = TextDefaults.defaultTextStyle.copy(
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )

                    Text(
                        text = stream.category.name,
                        style = TextDefaults.defaultTextStyle.copy(
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}
