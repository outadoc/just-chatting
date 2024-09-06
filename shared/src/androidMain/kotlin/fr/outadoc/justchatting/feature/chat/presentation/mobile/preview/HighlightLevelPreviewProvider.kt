package fr.outadoc.justchatting.feature.chat.presentation.mobile.preview

import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem.Message.Highlighted.Level
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

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
