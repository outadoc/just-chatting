package fr.outadoc.justchatting.feature.chat.presentation

import android.content.Context
import androidx.core.graphics.drawable.IconCompat
import fr.outadoc.justchatting.component.twitch.model.User

fun User.getProfileImageIcon(context: Context): IconCompat =
    IconCompat.createWithContentUri(
        UserProfileImageContentProvider.createForUser(context, login)
    )
