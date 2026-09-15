package fr.outadoc.justchatting.feature.shared.presentation.glance

import android.content.Context
import androidx.glance.ImageProvider
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.shared.ui.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

/**
 * Fixtures for the widget previews the launcher shows in its widget picker, published by
 * [publishWidgetPreviews].
 *
 * These are the same fictional channels the app's demo mode uses, so that a preview looks like the
 * app does before it has any of the user's own data. Nothing here may touch the network, the
 * database or Koin: a preview composition is run once and translated to `RemoteViews` on the spot,
 * with no chance to load anything.
 *
 * That also goes for the profile pictures. The real ones are content URIs served by
 * `UserProfileImageContentProvider`, which would have to be downloaded first, so previews use
 * bundled drawables instead - see [profileImage].
 *
 * The same channels appear in `res/layout/widget_preview_*.xml`, the static stand-ins the picker
 * falls back to when there is no generated preview. Both read the names and the drawables from the
 * same resources, so the two cannot drift apart.
 */
internal object WidgetPreviewData {
    private const val SOLANUM_ID = "preview-solanum"
    private const val YARROW_ID = "preview-yarrow"
    private const val POKE_ID = "preview-poke"

    private val profileImages: Map<String, Int> =
        mapOf(
            SOLANUM_ID to R.drawable.ic_preview_avatar_solanum,
            YARROW_ID to R.drawable.ic_preview_avatar_yarrow,
            POKE_ID to R.drawable.ic_preview_avatar_poke,
        )

    fun users(context: Context): ImmutableList<User> =
        persistentListOf(
            solanum(context),
            yarrow(context),
            poke(context),
        )

    fun liveStreams(context: Context): ImmutableList<UserStream> {
        val category =
            StreamCategory(
                id = "preview-category",
                name = context.getString(R.string.widget_preview_category),
            )

        return persistentListOf(
            previewStream(
                user = solanum(context),
                category = category,
                title = context.getString(R.string.widget_preview_stream_solanum),
            ),
            previewStream(
                user = yarrow(context),
                category = category,
                title = context.getString(R.string.widget_preview_stream_yarrow),
            ),
        )
    }

    /** The bundled stand-in for [user]'s profile picture, or the generic placeholder. */
    fun profileImage(user: User): ImageProvider = ImageProvider(profileImages[user.id] ?: R.drawable.ic_avatar_placeholder)

    private fun solanum(context: Context) = previewUser(SOLANUM_ID, "solanum", context.getString(R.string.widget_preview_channel_solanum))

    private fun yarrow(context: Context) = previewUser(YARROW_ID, "yarrow", context.getString(R.string.widget_preview_channel_yarrow))

    private fun poke(context: Context) = previewUser(POKE_ID, "poke", context.getString(R.string.widget_preview_channel_poke))

    private fun previewUser(
        id: String,
        login: String,
        displayName: String,
    ) = User(
        id = id,
        login = login,
        displayName = displayName,
        // Widgets show none of these, and a preview has no meaningful "now" to anchor the dates to.
        description = "",
        profileImageUrl = "",
        createdAt = Instant.DISTANT_PAST,
        usedAt = null,
    )

    private fun previewStream(
        user: User,
        category: StreamCategory,
        title: String,
    ) = UserStream(
        user = user,
        stream =
            Stream(
                id = "preview-stream-${user.id}",
                userId = user.id,
                category = category,
                title = title,
                viewerCount = 0,
                startedAt = Instant.DISTANT_PAST,
            ),
    )
}
