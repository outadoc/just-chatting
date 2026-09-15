package fr.outadoc.justchatting.feature.timeline.presentation.widget

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
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.PreviewSizeMode
import androidx.glance.appwidget.SizeMode
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
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.glance.GlanceCard
import fr.outadoc.justchatting.feature.shared.presentation.glance.WidgetPreviewData
import fr.outadoc.justchatting.feature.shared.presentation.glance.rememberProfileImageProvider
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainActivity
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.feature.timeline.presentation.LiveTimelineViewModel
import fr.outadoc.justchatting.shared.ui.R
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.koinInject

internal class LiveWidget : GlanceAppWidget() {
    companion object {
        private val HORIZONTAL_RECTANGLE = DpSize(250.dp, 110.dp)
    }

    // The picker shows a single image, so compose it at the size the widget asks to be placed at
    // (4x2 cells, per widget_live.xml) rather than at its minimum, which fits no stream at all.
    override val previewSizeMode: PreviewSizeMode =
        SizeMode.Responsive(setOf(HORIZONTAL_RECTANGLE))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            val viewModel: LiveTimelineViewModel = koinInject()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.syncLiveStreamsNow()
                viewModel.syncLiveStreamsPeriodically()
            }

            Content(
                streams = state.live,
                onRefresh = viewModel::syncLiveStreamsNow,
            )
        }
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        provideContent {
            Content(
                streams = WidgetPreviewData.liveStreams(LocalContext.current),
                onRefresh = {},
                profileImage = { user -> WidgetPreviewData.profileImage(user) },
            )
        }
    }

    @Composable
    private fun Content(
        streams: ImmutableList<UserStream>,
        onRefresh: () -> Unit,
        profileImage: @Composable (User) -> ImageProvider = { user -> rememberProfileImageProvider(user) },
    ) {
        GlanceTheme(colors = GlanceTheme.colors) {
            Scaffold(
                titleBar = {
                    TitleBar(
                        startIcon = ImageProvider(R.drawable.ic_notif),
                        title = LocalContext.current.getString(R.string.widget_live_title),
                        actions = {
                            CircleIconButton(
                                modifier = GlanceModifier.padding(8.dp),
                                imageProvider = ImageProvider(R.drawable.ic_sync),
                                contentDescription = LocalContext.current.getString(R.string.widget_refresh_action_cd),
                                backgroundColor = null,
                                key = "refresh",
                                onClick = onRefresh,
                            )
                        },
                    )
                },
            ) {
                LazyColumn {
                    items(streams) { userStream ->
                        Column {
                            GlanceCard(
                                modifier =
                                    GlanceModifier
                                        .clickable(
                                            MainActivity.createGlanceAction(
                                                userId = userStream.user.id,
                                            ),
                                        ),
                            ) {
                                LiveStream(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    user = userStream.user,
                                    stream = userStream.stream,
                                    profileImage = profileImage(userStream.user),
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

    @Composable
    private fun LiveStream(
        modifier: GlanceModifier = GlanceModifier,
        user: User,
        stream: Stream,
        profileImage: ImageProvider,
    ) {
        Column(
            modifier = modifier,
        ) {
            Text(
                text = stream.title,
                style =
                    TextDefaults.defaultTextStyle.copy(
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
                    provider = profileImage,
                    contentDescription = null,
                )

                Spacer(
                    modifier = GlanceModifier.width(4.dp),
                )

                Text(
                    text = user.displayName,
                    style =
                        TextDefaults.defaultTextStyle.copy(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        ),
                    maxLines = 1,
                )

                stream.category?.let { category ->
                    Text(
                        text = " • ",
                        style =
                            TextDefaults.defaultTextStyle.copy(
                                color = GlanceTheme.colors.onSurfaceVariant,
                            ),
                    )

                    Text(
                        text = category.name,
                        style =
                            TextDefaults.defaultTextStyle.copy(
                                color = GlanceTheme.colors.onSurfaceVariant,
                            ),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
