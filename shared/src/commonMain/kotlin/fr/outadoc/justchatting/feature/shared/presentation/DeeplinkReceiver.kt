package fr.outadoc.justchatting.feature.shared.presentation

import com.eygraber.uri.Uri

public interface DeeplinkReceiver {
    public fun onDeeplinkReceived(uri: Uri)

    public fun onDeeplinkReceived(uriString: String)
}
