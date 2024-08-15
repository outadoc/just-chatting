package fr.outadoc.justchatting.feature.deeplink

import com.eygraber.uri.Uri

internal object DeeplinkDefinitions {
    val ViewChannel: Uri = Uri.parse("justchatting://user")
    val AuthCallback: Uri = Uri.parse("justchatting://auth/callback")
}
