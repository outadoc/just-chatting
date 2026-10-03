package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.ui.platform.UriHandler
import com.eygraber.uri.Uri
import kotlin.test.Test
import kotlin.test.assertEquals

class AppUriHandlerTest {
    private val platformUris = mutableListOf<String>()
    private val deeplinks = mutableListOf<Uri>()

    private val handler =
        AppUriHandler(
            platformUriHandler =
                object : UriHandler {
                    override fun openUri(uri: String) {
                        platformUris += uri
                    }
                },
            onDeeplinkReceived = { uri -> deeplinks += uri },
        )

    @Test
    fun `channel links are handled in-app`() {
        handler.openUri("justchatting://user/51958193")

        assertEquals(listOf(Uri.parse("justchatting://user/51958193")), deeplinks)
        assertEquals(emptyList(), platformUris)
    }

    @Test
    fun `other links are opened by the platform`() {
        handler.openUri("https://www.twitch.tv/sundae")

        assertEquals(emptyList(), deeplinks)
        assertEquals(listOf("https://www.twitch.tv/sundae"), platformUris)
    }
}
