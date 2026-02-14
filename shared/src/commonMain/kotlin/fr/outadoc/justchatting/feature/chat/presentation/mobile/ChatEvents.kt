package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Poll
import fr.outadoc.justchatting.feature.chat.domain.model.Prediction
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.chat.presentation.OngoingEvents
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.connectionLost_error
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Composable
internal fun ChatEvents(
    modifier: Modifier = Modifier,
    insets: PaddingValues,
    roomState: RoomState,
    isDisconnected: Boolean,
    ongoingEvents: OngoingEvents,
    clock: Clock,
    inlineContent: PersistentMap<String, InlineTextContent>,
    removedContent: ImmutableList<ChatListItem.RemoveContent>,
    appUser: AppUser.LoggedIn,
    badges: ImmutableList<TwitchBadge>,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Spacer(
            modifier =
            Modifier.padding(
                top = insets.calculateTopPadding(),
            ),
        )
        AnimatedVisibility(
            visible = roomState != RoomState.Default,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            RoomStateBanner(
                modifier = Modifier.fillMaxWidth(),
                roomState = roomState,
            )
        }

        AnimatedVisibility(
            visible = isDisconnected,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            SlimSnackbar(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Text(text = stringResource(Res.string.connectionLost_error))
            }
        }

        val pinnedMessage: OngoingEvents.PinnedMessage? = ongoingEvents.pinnedMessage
        val pinnedMessageEnd: Instant? = pinnedMessage?.endsAt

        IntervalCheckVisibility(
            visible = { pinnedMessage != null && (pinnedMessageEnd == null || clock.now() < pinnedMessageEnd + 1.minutes) },
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            if (pinnedMessage != null) {
                PinnedMessageCard(
                    modifier = Modifier.fillMaxWidth(),
                    message = pinnedMessage.message,
                    appUser = appUser,
                    inlineContent = inlineContent,
                    removedContent = removedContent,
                )
            }
        }

        val poll: Poll? = ongoingEvents.poll
        val pollEnd: Instant? = poll?.endedAt

        IntervalCheckVisibility(
            visible = { poll != null && (pollEnd == null || clock.now() < pollEnd + 1.minutes) },
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            if (poll != null) {
                PollCard(
                    modifier = Modifier.fillMaxWidth(),
                    poll = poll,
                )
            }
        }

        val prediction: Prediction? = ongoingEvents.prediction
        val predictionEnd: Instant? = prediction?.endedAt

        IntervalCheckVisibility(
            visible = { prediction != null && (predictionEnd == null || clock.now() < predictionEnd + 1.minutes) },
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            if (prediction != null) {
                PredictionCard(
                    modifier = Modifier.fillMaxWidth(),
                    prediction = prediction,
                    badges = badges,
                )
            }
        }

        val raid: Raid? = ongoingEvents.outgoingRaid

        AnimatedVisibility(
            visible = raid != null,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            when (raid) {
                is Raid.Go -> {
                    RaidGoCard(
                        modifier = modifier,
                        raid = raid,
                    )
                }

                is Raid.Preparing -> {
                    RaidPrepareCard(
                        modifier = modifier,
                        raid = raid,
                    )
                }

                null -> {}
            }
        }
    }
}
