package fr.outadoc.justchatting

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.outadoc.justchatting.di.startSharedKoin
import fr.outadoc.justchatting.feature.shared.presentation.mobile.App

public fun startApp() {
    startSharedKoin()
    application {
        Window(
            title = "Just Chatting",
            onCloseRequest = ::exitApplication
        ) {
            App()
        }
    }
}
