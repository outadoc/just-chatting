package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.chat.domain.ChatRepository
import fr.outadoc.justchatting.feature.chat.domain.DefaultChatRepository
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.demo.domain.DemoModeRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
internal class DemoAwareChatRepository(
    private val demoModeRepository: DemoModeRepository,
    private val real: Lazy<DefaultChatRepository>,
    private val demo: DemoChatRepository,
) : ChatRepository {
    override fun getChatEventFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> =
        demoModeRepository.isDemoMode.flatMapLatest { isDemoMode ->
            (if (isDemoMode) demo else real.value).getChatEventFlow(user, appUser)
        }

    override fun getConnectionStatusFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ConnectionStatus> =
        demoModeRepository.isDemoMode.flatMapLatest { isDemoMode ->
            (if (isDemoMode) demo else real.value).getConnectionStatusFlow(user, appUser)
        }
}
