package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.app.Activity
import android.app.ActivityManager
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import fr.outadoc.justchatting.feature.home.domain.model.User

@Composable
internal fun UpdateTaskDescriptionForUser(user: User?) {
    val context = LocalContext.current as? Activity ?: return
    LaunchedEffect(user) {
        if (user != null) {
            val request = ImageRequest.Builder(context)
                .data(user.profileImageUrl)
                .allowHardware(false)
                .size(128)
                .target { drawable ->
                    val bitmap = (drawable as? BitmapDrawable)?.bitmap
                        ?: return@target

                    context.setTaskDescription(
                        ActivityManager.TaskDescription(user.displayName, bitmap),
                    )
                }
                .build()

            context.imageLoader.enqueue(request)
        }
    }
}
