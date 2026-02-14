package fr.outadoc.justchatting.feature.chat.presentation

import android.content.Context
import android.net.Uri
import androidx.core.graphics.drawable.IconCompat
import fr.outadoc.justchatting.feature.shared.domain.model.User

internal fun User.getProfileImageUri(context: Context): Uri = UserProfileImageContentProvider.createForUser(context, id)

internal fun User.getProfileImageIcon(context: Context): IconCompat = IconCompat.createWithContentUri(
    getProfileImageUri(context),
)
