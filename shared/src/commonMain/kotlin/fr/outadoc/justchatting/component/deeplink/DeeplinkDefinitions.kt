package fr.outadoc.justchatting.component.deeplink

import com.eygraber.uri.Uri

internal object DeeplinkDefinitions {
    val ViewChannel: Uri = Uri.parse("justchatting://channel")
    val AuthCallback: Uri = Uri.parse("justchatting://auth/callback")
}
