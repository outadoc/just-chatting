package fr.outadoc.justchatting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.application
import fr.outadoc.justchatting.di.startSharedKoin
import fr.outadoc.justchatting.utils.logging.LogStrategy
import fr.outadoc.justchatting.utils.logging.Logger

public fun startApp() {
    val koinApp = startSharedKoin()
    Logger.logStrategy = koinApp.koin.get<LogStrategy>()

    application {
        // Each entry represents one open window; removing an id closes its window.
        // The app exits once the last window is closed.
        val windowIds = remember { mutableStateListOf(0L) }
        var nextWindowId by remember { mutableStateOf(1L) }

        windowIds.toList().forEach { windowId ->
            key(windowId) {
                AppWindow(
                    onCloseRequest = {
                        windowIds.remove(windowId)
                        if (windowIds.isEmpty()) {
                            exitApplication()
                        }
                    },
                    onNewWindowRequest = {
                        windowIds.add(nextWindowId)
                        nextWindowId++
                    },
                )
            }
        }
    }
}
