package fr.outadoc.justchatting.utils.core

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.shared.domain.model.User

public fun createChannelExternalLink(user: User): String =
    Uri
        .parse("https://twitch.tv")
        .buildUpon()
        .appendPath(user.login)
        .build()
        .toString()

public fun createVideoExternalLink(videoId: String): String =
    Uri
        .parse("https://www.twitch.tv/videos")
        .buildUpon()
        .appendPath(videoId)
        .build()
        .toString()
