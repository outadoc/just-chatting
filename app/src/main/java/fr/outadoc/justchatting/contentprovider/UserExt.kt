package fr.outadoc.justchatting.contentprovider

import androidx.core.graphics.drawable.IconCompat
import fr.outadoc.justchatting.model.helix.user.User

val User.profileImageIcon: IconCompat
    get() = IconCompat.createWithContentUri(
        UserProfileImageContentProvider.createForUser(login)
    )
