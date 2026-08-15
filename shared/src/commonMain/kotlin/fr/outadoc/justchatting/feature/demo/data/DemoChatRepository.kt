package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.chat.domain.ChatRepository
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class DemoChatRepository(
    private val clock: Clock,
    private val demoChatBus: DemoChatBus,
) : ChatRepository {
    @OptIn(ExperimentalUuidApi::class)
    override fun getChatEventFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> {
        val initialState =
            flowOf(
                ChatEvent.Command.RoomStateDelta(),
                ChatEvent.Command.UserState(emoteSets = listOf("0")),
                ChatEvent.Message.Join(timestamp = clock.now(), channelLogin = user.login),
                ChatEvent.Message.BroadcastSettingsUpdate(
                    timestamp = clock.now(),
                    streamTitle = "Building a demo mode, of all things",
                    categoryId = "demo-category",
                    categoryName = "Software and Game Development",
                ),
                ChatEvent.Message.ViewerCountUpdate(timestamp = clock.now(), viewerCount = 42),
            )

        val openingBurst =
            flow {
                DemoData.chatOpeningBurst.forEach { build ->
                    emit(build(clock.now(), Uuid.random().toString()))
                }
            }

        val scriptedLoop =
            flow {
                while (true) {
                    DemoData.chatScript.forEach { entry ->
                        emit(entry.build(clock.now(), Uuid.random().toString()))
                        delay(entry.delayAfter)
                    }
                }
            }

        val echoedMessages =
            demoChatBus.events
                .filter { (channelId, _) -> channelId == user.id }
                .map { (_, event) -> event }

        return merge(initialState, openingBurst, scriptedLoop, echoedMessages)
    }

    override fun getConnectionStatusFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ConnectionStatus> =
        flowOf(
            ConnectionStatus(
                isAlive = true,
                registeredListeners = 1,
                aliveConnections = 1,
            ),
        )
}
