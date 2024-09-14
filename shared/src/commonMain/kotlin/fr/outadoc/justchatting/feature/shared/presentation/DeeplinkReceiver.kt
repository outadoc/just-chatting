package fr.outadoc.justchatting.feature.shared.presentation

import com.eygraber.uri.Uri

public interface DeeplinkReceiver {
    public fun onReceiveIntent(uri: Uri)
    public fun onReceiveIntent(uri: String)
}
