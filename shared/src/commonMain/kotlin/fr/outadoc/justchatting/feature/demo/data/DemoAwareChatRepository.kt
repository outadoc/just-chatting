package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.chat.domain.ChatRepository
import fr.outadoc.justchatting.feature.chat.domain.DefaultChatRepository
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.demo.domain.DemoModeRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.coroutines.flow.Flow

internal class DemoAwareChatRepository(
    private val demoModeRepository: DemoModeRepository,
    private val real: Lazy<DefaultChatRepository>,
    private val demo: DemoChatRepository,
) : ChatRepository {
    private fun current(): ChatRepository = if (demoModeRepository.isDemoMode.value) demo else real.value

    override fun getChatEventFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> = current().getChatEventFlow(user, appUser)

    override fun getConnectionStatusFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ConnectionStatus> = current().getConnectionStatusFlow(user, appUser)
}
