package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.ui.AndroidChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.ui.AndroidCreateShortcutForChannelUseCase
import org.koin.dsl.module

internal val androidUiModule =
    module {
        single<ChatNotifier> { AndroidChatNotifier(get(), get()) }
        single<CreateShortcutForChannelUseCase> { AndroidCreateShortcutForChannelUseCase(get()) }
    }
