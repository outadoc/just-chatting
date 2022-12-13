package fr.outadoc.justchatting.feature.chat.presentation

import androidx.core.graphics.drawable.IconCompat
import fr.outadoc.justchatting.component.twitch.model.User

val User.profileImageIcon: IconCompat
    get() = IconCompat.createWithContentUri(
        UserProfileImageContentProvider.createForUser(login)
    )
