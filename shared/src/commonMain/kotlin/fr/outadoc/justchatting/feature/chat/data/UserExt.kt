package fr.outadoc.justchatting.feature.chat.data

import android.content.Context
import androidx.core.graphics.drawable.IconCompat
import fr.outadoc.justchatting.component.chatapi.domain.model.User

fun User.getProfileImageIcon(context: Context): IconCompat =
    IconCompat.createWithContentUri(
        UserProfileImageContentProvider.createForUser(context, login).toString(),
    )
