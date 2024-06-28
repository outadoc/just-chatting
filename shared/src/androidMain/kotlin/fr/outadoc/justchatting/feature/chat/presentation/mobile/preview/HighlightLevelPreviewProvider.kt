package fr.outadoc.justchatting.feature.chat.presentation.mobile.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent.Message.Highlighted.Level

internal class HighlightLevelPreviewProvider : PreviewParameterProvider<Level> {
    override val values: Sequence<Level>
        get() = sequence {
            yield(Level.Base)
            yield(Level.One)
            yield(Level.Two)
            yield(Level.Three)
            yield(Level.Four)
            yield(Level.Five)
            yield(Level.Six)
            yield(Level.Seven)
            yield(Level.Eight)
            yield(Level.Nine)
            yield(Level.Ten)
        }
}
