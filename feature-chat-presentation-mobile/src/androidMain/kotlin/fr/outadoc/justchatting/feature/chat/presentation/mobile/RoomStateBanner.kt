package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
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
                Text(text = stringResource(R.string.room_emote))
            }

            if (!minFollowDuration.isNegative()) {
                Text(
                    text = when (minFollowDuration) {
                        Duration.ZERO -> stringResource(R.string.room_followers)
                        else -> stringResource(
                            R.string.room_followers_min,
                            minFollowDuration.format(LocalContext.current),
                        )
                    },
                )
            }

            if (uniqueMessagesOnly) {
                Text(text = stringResource(R.string.room_unique))
            }

            if (slowModeDuration.isPositive()) {
                Text(
                    text = stringResource(
                        R.string.room_slow,
                        slowModeDuration.format(LocalContext.current),
                    ),
                )
            }

            if (isSubOnly) {
                Text(text = stringResource(R.string.room_subs))
            }
        }
    }
}