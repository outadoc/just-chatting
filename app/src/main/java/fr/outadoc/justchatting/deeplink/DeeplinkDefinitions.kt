package fr.outadoc.justchatting.deeplink

import android.net.Uri

object DeeplinkDefinitions {
    val ViewChannel: Uri = Uri.parse("justchatting://channel")
    val AuthCallback: Uri = Uri.parse("justchatting://auth/callback")
}