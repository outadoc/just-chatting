package fr.outadoc.justchatting.logging

import fr.outadoc.justchatting.utils.logging.logDebug
import io.ktor.util.AttributeKey
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketExtension
import io.ktor.websocket.WebSocketExtensionFactory
import io.ktor.websocket.WebSocketExtensionHeader
import io.ktor.websocket.readText

class FrameLogger : WebSocketExtension<FrameLogger.Config> {

    class Config

    override val factory: WebSocketExtensionFactory<Config, out WebSocketExtension<Config>>
        get() = Companion

    override val protocols: List<WebSocketExtensionHeader> = emptyList()

    override fun clientNegotiation(negotiatedProtocols: List<WebSocketExtensionHeader>): Boolean {
        return true
    }

    override fun processIncomingFrame(frame: Frame): Frame {
        logDebug<FrameLogger> {
            when (frame) {
                is Frame.Binary -> "received binary frame with len=${frame.data.size}"
                is Frame.Close -> "received close frame"
                is Frame.Ping -> "received ping frame"
                is Frame.Pong -> "received pong frame"
                is Frame.Text -> "received text frame: ${frame.readText()}"
            }
        }

        return frame
    }

    override fun processOutgoingFrame(frame: Frame): Frame {
        logDebug<FrameLogger> {
            when (frame) {
                is Frame.Binary -> "sending binary frame with len=${frame.data.size}"
                is Frame.Close -> "sending close frame"
                is Frame.Ping -> "sending ping frame"
                is Frame.Pong -> "sending pong frame"
                is Frame.Text -> "sending text frame: ${frame.readText()}"
            }
        }

        return frame
    }

    override fun serverNegotiation(
        requestedProtocols: List<WebSocketExtensionHeader>,
    ): List<WebSocketExtensionHeader> {
        return emptyList()
    }

    companion object : WebSocketExtensionFactory<Config, FrameLogger> {

        override val key: AttributeKey<FrameLogger> = AttributeKey("frame-logger")

        override val rsv1: Boolean = false
        override val rsv2: Boolean = false
        override val rsv3: Boolean = false

        override fun install(config: Config.() -> Unit): FrameLogger = FrameLogger()
    }
}
