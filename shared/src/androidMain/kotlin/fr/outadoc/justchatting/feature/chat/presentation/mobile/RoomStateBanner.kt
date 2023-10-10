package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.ui.format
import kotlin.time.Duration

@Composable
fun RoomStateBanner(
    modifier: Modifier = Modifier,
    roomState: RoomState,
) {
    SlimSnackbar(modifier = modifier) {
        with(roomState) {
            if (isEmoteOnly) {
                Text(text = stringResource(MR.strings.room_emote))
            }

            if (!minFollowDuration.isNegative()) {
                Text(
                    text = when (minFollowDuration) {
                        Duration.ZERO -> stringResource(MR.strings.room_followers)
                        else -> stringResource(
                            MR.strings.room_followers_min,
                            minFollowDuration.format(LocalContext.current),
                        )
                    },
                )
            }

            if (uniqueMessagesOnly) {
                Text(text = stringResource(MR.strings.room_unique))
            }

            if (slowModeDuration.isPositive()) {
                Text(
                    text = stringResource(
                        MR.strings.room_slow,
                        slowModeDuration.format(LocalContext.current),
                    ),
                )
            }

            if (isSubOnly) {
                Text(text = stringResource(MR.strings.room_subs))
            }
        }
    }
}
