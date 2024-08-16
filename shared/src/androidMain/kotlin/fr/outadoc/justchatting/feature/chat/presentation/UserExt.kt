package fr.outadoc.justchatting.feature.chat.presentation

import android.content.Context
import androidx.core.graphics.drawable.IconCompat
import fr.outadoc.justchatting.feature.home.domain.model.User

internal fun User.getProfileImageIcon(context: Context): IconCompat =
    IconCompat.createWithContentUri(
        UserProfileImageContentProvider.createForUser(context, id).toString(),
    )
