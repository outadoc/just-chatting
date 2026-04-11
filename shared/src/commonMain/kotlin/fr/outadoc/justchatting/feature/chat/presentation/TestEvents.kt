package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.PinnedMessage
import fr.outadoc.justchatting.feature.chat.domain.model.Poll
import fr.outadoc.justchatting.feature.chat.domain.model.Prediction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal object TestEvents {
    val events: Flow<ChatViewModel.Action> =
        flowOf(
            ChatViewModel.Action.UpdatePrediction(
                prediction =
                    Prediction(
                        id = "0c64f437-7481-46a3-9d80-2834cc415dfc",
                        title = "ANTOINE GAGNE ?",
                        status = Prediction.Status.Active,
                        createdAt = Instant.parse("2023-02-08T20:34:35.839478452Z"),
                        endedAt = null,
                        lockedAt = null,
                        outcomes =
                            listOf(
                                Prediction.Outcome(
                                    id = "1df7ac61-7912-4c82-89d3-d7781c0c182b",
                                    title = "OUI",
                                    color = "#1e69ff",
                                    totalPoints = 50,
                                    totalUsers = 0,
                                    badge = Badge(id = "predictions", version = "blue-1"),
                                ),
                                Prediction.Outcome(
                                    id = "a6225650-401b-4874-83aa-839f747d5f55",
                                    title = "NON",
                                    color = "#e0008e",
                                    totalPoints = 100,
                                    totalUsers = 0,
                                    badge = Badge(id = "predictions", version = "pink-2"),
                                ),
                            ),
                        predictionWindow = 2.minutes,
                        winningOutcome = null,
                    ),
            ),
            ChatViewModel.Action.UpdatePoll(
                poll =
                    Poll(
                        pollId = "0c64f437-7481-46a3-9d80-2834cc415dfc",
                        title = "ANTOINE GAGNE ?",
                        status = Poll.Status.Active,
                        startedAt = Instant.parse("2023-02-08T20:34:35.839478452Z"),
                        endedAt = null,
                        choices =
                            listOf(
                                Poll.Choice(
                                    choiceId = "1",
                                    title = "Étoiles",
                                    votes =
                                        Poll.Votes(
                                            total = 12345,
                                            bits = 123,
                                            channelPoints = 50,
                                            base = 1412,
                                        ),
                                    totalVoters = 1000,
                                ),
                                Poll.Choice(
                                    choiceId = "1",
                                    title = "AntoineDaniel",
                                    votes =
                                        Poll.Votes(
                                            total = 102345,
                                            bits = 123,
                                            channelPoints = 50,
                                            base = 1412,
                                        ),
                                    totalVoters = 1000,
                                ),
                                Poll.Choice(
                                    choiceId = "1",
                                    title = "HortyUnderscore",
                                    votes =
                                        Poll.Votes(
                                            total = 52450,
                                            bits = 123,
                                            channelPoints = 50,
                                            base = 1412,
                                        ),
                                    totalVoters = 1000,
                                ),
                            ),
                        duration = 3.minutes,
                        remainingDuration = 53.seconds,
                        totalVoters = 133143,
                        votes =
                            Poll.Votes(
                                total = 134356,
                                bits = 1311,
                                channelPoints = 2345,
                                base = 757,
                            ),
                    ),
            ),
            ChatViewModel.Action.AddMessages(
                messages =
                    listOf(
                        ChatListItem.Message.Simple(
                            timestamp = Instant.parse("2023-02-08T20:35:35.839478452Z"),
                            body =
                                ChatListItem.Message.Body(
                                    messageId = "14124",
                                    message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
                                    chatter =
                                        Chatter(
                                            id = "1462345",
                                            login = "autobot",
                                            displayName = "Autobot",
                                        ),
                                ),
                        ),
                    ),
            ),
            ChatViewModel.Action.UpdatePinnedMessage(
                pinnedMessage =
                    PinnedMessage(
                        pinId = "1342345235",
                        pinnedBy =
                            PinnedMessage.User(
                                userId = "1462345",
                                displayName = "Autobot",
                            ),
                        message =
                            PinnedMessage.Message(
                                messageId = "14124",
                                sender =
                                    PinnedMessage.User(
                                        userId = "1462345",
                                        displayName = "Autobot",
                                    ),
                                content =
                                    PinnedMessage.Message.Content(
                                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
                                    ),
                                startsAt = Instant.DISTANT_PAST,
                                endsAt = Instant.DISTANT_FUTURE,
                            ),
                    ),
            ),
        )
}
