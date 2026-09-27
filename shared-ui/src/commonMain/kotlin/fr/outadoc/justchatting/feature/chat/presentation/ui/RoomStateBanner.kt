package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.presentation.RoomMode
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
import fr.outadoc.justchatting.feature.chat.presentation.activeModes
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.room_emote
import fr.outadoc.justchatting.shared.internal.room_followers
import fr.outadoc.justchatting.shared.internal.room_followers_min
import fr.outadoc.justchatting.shared.internal.room_slow
import fr.outadoc.justchatting.shared.internal.room_subs
import fr.outadoc.justchatting.shared.internal.room_unique
import fr.outadoc.justchatting.utils.presentation.format
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

@Composable
internal fun RoomStateBanner(
    modifier: Modifier = Modifier,
    roomState: RoomState,
) {
    SlimSnackbar(modifier = modifier) {
        roomState.activeModes.forEach { mode ->
            Text(
                text =
                    when (mode) {
                        RoomMode.EmoteOnly -> {
                            stringResource(Res.string.room_emote)
                        }

                        is RoomMode.FollowersOnly -> {
                            if (mode.minFollowDuration == Duration.ZERO) {
                                stringResource(Res.string.room_followers)
                            } else {
                                stringResource(Res.string.room_followers_min, mode.minFollowDuration.format())
                            }
                        }

                        RoomMode.UniqueMessages -> {
                            stringResource(Res.string.room_unique)
                        }

                        is RoomMode.Slow -> {
                            stringResource(Res.string.room_slow, mode.delay.format())
                        }

                        RoomMode.SubscribersOnly -> {
                            stringResource(Res.string.room_subs)
                        }
                    },
            )
        }
    }
}
