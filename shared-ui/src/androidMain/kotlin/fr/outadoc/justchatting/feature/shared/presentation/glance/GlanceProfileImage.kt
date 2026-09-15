package fr.outadoc.justchatting.feature.shared.presentation.glance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.ImageProvider
import fr.outadoc.justchatting.feature.chat.presentation.ProfileImageCache
import fr.outadoc.justchatting.feature.chat.presentation.getProfileImageUri
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.shared.ui.R
import org.koin.compose.koinInject

/**
 * Provides the profile picture of [user], downloading it first if needed.
 *
 * A content URI is only handed out once the picture is on disk. Widgets are rendered by the
 * launcher, which reads that URI synchronously while applying our `RemoteViews`, so pointing it at
 * an image we have yet to download would freeze it for the duration. Downloading here instead keeps
 * the work in our own process, on the widget's coroutine.
 *
 * A placeholder stands in until then. It is also what makes the picture appear at all: `ImageView`
 * ignores `setImageURI` when the URI has not changed, so swapping the drawable out is what gets the
 * launcher to load the real image once we have it.
 */
@Composable
internal fun rememberProfileImageProvider(user: User): ImageProvider {
    val context = LocalContext.current
    val profileImageCache: ProfileImageCache = koinInject()

    var isCached: Boolean by remember(user.id) {
        mutableStateOf(profileImageCache.isCached(user.id))
    }

    LaunchedEffect(user.id) {
        if (!isCached) {
            isCached = profileImageCache.fetch(user.id)
        }
    }

    return if (isCached) {
        ImageProvider(user.getProfileImageUri(context))
    } else {
        ImageProvider(R.drawable.ic_avatar_placeholder)
    }
}
