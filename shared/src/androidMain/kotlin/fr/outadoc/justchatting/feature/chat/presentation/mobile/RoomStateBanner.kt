package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.room_emote
import fr.outadoc.justchatting.shared.room_followers
import fr.outadoc.justchatting.shared.room_followers_min
import fr.outadoc.justchatting.shared.room_slow
import fr.outadoc.justchatting.shared.room_subs
import fr.outadoc.justchatting.shared.room_unique
import fr.outadoc.justchatting.utils.ui.format
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

@Composable
fun RoomStateBanner(
    modifier: Modifier = Modifier,
    roomState: RoomState,
) {
    SlimSnackbar(modifier = modifier) {
        with(roomState) {
            if (isEmoteOnly) {
                Text(text = stringResource(Res.string.room_emote))
            }

            if (!minFollowDuration.isNegative()) {
                Text(
                    text = when (minFollowDuration) {
                        Duration.ZERO -> stringResource(Res.string.room_followers)
                        else -> stringResource(
                            Res.string.room_followers_min,
                            minFollowDuration.format(),
                        )
                    },
                )
            }

            if (uniqueMessagesOnly) {
                Text(text = stringResource(Res.string.room_unique))
            }

            if (slowModeDuration.isPositive()) {
                Text(
                    text = stringResource(
                        Res.string.room_slow,
                        slowModeDuration.format(),
                    ),
                )
            }

            if (isSubOnly) {
                Text(text = stringResource(Res.string.room_subs))
            }
        }
    }
}
