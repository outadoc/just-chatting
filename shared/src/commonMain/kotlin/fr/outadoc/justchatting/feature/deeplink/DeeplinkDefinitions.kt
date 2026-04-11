package fr.outadoc.justchatting.feature.deeplink

import com.eygraber.uri.Uri

public object DeeplinkDefinitions {
    public val ViewChannel: Uri = Uri.parse("justchatting://user")
    public val AuthCallback: Uri = Uri.parse("justchatting://auth/callback")
}
