package fr.outadoc.justchatting.component.twitch.websocket.irc

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.outlined.Star
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.twitch.R
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.IrcEvent
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.format
import kotlinx.collections.immutable.toImmutableList
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency

class IrcMessageMapper(private val context: Context) {

    fun mapMessage(ircEvent: IrcEvent.Message): ChatEvent = with(ircEvent) {
        when (this) {
            is IrcEvent.Message.Notice -> {
                ChatEvent.Message.Notice(
                    timestamp = timestamp,
                    text = getLabelForNotice(
                        messageId = messageId,
                        message = message,
                    ) ?: message,
                )
            }

            is IrcEvent.Message.IncomingRaid -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Default.CallReceived,
                        subtitle = context.getString(
                            R.string.chat_raid_header,
                            context.resources.getQuantityString(
                                R.plurals.viewers,
                                raidersCount,
                                raidersCount,
                            ),
                        ),
                    ),
                    body = null,
                )
            }

            is IrcEvent.Message.CancelledRaid -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Default.Cancel,
                        subtitle = context.getString(R.string.chat_unraid_subtitle),
                    ),
                    body = null,
                )
            }

            is IrcEvent.Message.HighlightedMessage -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = context.getString(R.string.irc_msgid_highlighted_message),
                        titleIcon = Icons.Default.Highlight,
                        subtitle = null,
                    ),
                    body = userMessage.map(),
                )
            }

            is IrcEvent.Message.Announcement -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = context.getString(R.string.irc_msgid_announcement),
                        titleIcon = Icons.Default.Campaign,
                        subtitle = null,
                    ),
                    body = userMessage.map(),
                )
            }

            is IrcEvent.Message.Subscription -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = when (subscriptionPlan) {
                            SUB_PRIME -> Icons.Outlined.Star
                            else -> Icons.Filled.Star
                        },
                        subtitle = when (streakMonths) {
                            0 -> context.getString(
                                R.string.chat_sub_header_withDuration,
                                parseSubscriptionTier(subscriptionPlan),
                                context.resources.getQuantityString(
                                    R.plurals.months,
                                    cumulativeMonths,
                                    cumulativeMonths.formatNumber(),
                                ),
                            )

                            else -> context.getString(
                                R.string.chat_sub_header_withDurationAndStreak,
                                parseSubscriptionTier(subscriptionPlan),
                                context.resources.getQuantityString(
                                    R.plurals.months,
                                    cumulativeMonths,
                                    cumulativeMonths.formatNumber(),
                                ),
                                context.resources.getQuantityString(
                                    R.plurals.months,
                                    streakMonths,
                                    streakMonths.formatNumber(),
                                ),
                            )
                        },
                    ),
                    body = userMessage?.map(),
                )
            }

            is IrcEvent.Message.SubscriptionConversion -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Filled.Star,
                        subtitle = context.getString(
                            R.string.chat_subConversion_header,
                            parseSubscriptionTierWithArticle(subscriptionPlan),
                        ),
                    ),
                    body = userMessage?.map(),
                )
            }

            is IrcEvent.Message.MassSubscriptionGift -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Default.VolunteerActivism,
                        subtitle = context.getString(
                            R.string.chat_massSubGift_header,
                            giftCount.formatNumber(),
                            parseSubscriptionTierWithArticle(subscriptionPlan),
                            totalChannelGiftCount.formatNumber(),
                        ),
                    ),
                    body = null,
                )
            }

            is IrcEvent.Message.SubscriptionGift -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    body = null,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Default.Redeem,
                        subtitle = context.getString(
                            R.string.chat_subGift_header,
                            parseSubscriptionTier(subscriptionPlan),
                            recipientDisplayName,
                            context.resources.getQuantityString(
                                R.plurals.months,
                                cumulativeMonths,
                                cumulativeMonths.formatNumber(),
                            ),
                        ),
                    ),
                )
            }

            is IrcEvent.Message.GiftPayForward -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Default.FastForward,
                        subtitle = when (priorGifterDisplayName) {
                            null -> context.getString(R.string.chat_subGift_payForwardAnonymous)
                            else -> {
                                context.getString(
                                    R.string.chat_subGift_payForward,
                                    priorGifterDisplayName,
                                )
                            }
                        },
                    ),
                    body = null,
                )
            }

            is IrcEvent.Message.UserNotice -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    body = userMessage?.map(),
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = systemMsg,
                        titleIcon = null,
                        subtitle = null,
                    ),
                )
            }

            is IrcEvent.Message.ChatMessage -> {
                val metadata = when {
                    isFirstMessageByUser -> {
                        ChatEvent.Message.Highlighted.Metadata(
                            title = context.getString(R.string.chat_first),
                            titleIcon = Icons.Default.WavingHand,
                            subtitle = null,
                        )
                    }

                    rewardId != null -> {
                        ChatEvent.Message.Highlighted.Metadata(
                            title = context.getString(R.string.chat_reward),
                            titleIcon = Icons.Default.Toll,
                            subtitle = null,
                        )
                    }

                    paidMessageInfo != null -> {
                        val currency = Currency.getInstance(paidMessageInfo.currency)
                        val amount = BigDecimal.valueOf(
                            paidMessageInfo.amount,
                            paidMessageInfo.exponent,
                        )

                        val formattedAmount =
                            NumberFormat.getCurrencyInstance()
                                .apply { this.currency = currency }
                                .format(amount)

                        val header = context.getString(
                            R.string.chat_paidMessage,
                            formattedAmount,
                        )

                        ChatEvent.Message.Highlighted.Metadata(
                            title = header,
                            titleIcon = Icons.Default.Bolt,
                            subtitle = null,
                            level = when (paidMessageInfo.level) {
                                "ONE" -> ChatEvent.Message.Highlighted.Level.One
                                "TWO" -> ChatEvent.Message.Highlighted.Level.Two
                                "THREE" -> ChatEvent.Message.Highlighted.Level.Three
                                "FOUR" -> ChatEvent.Message.Highlighted.Level.Four
                                "FIVE" -> ChatEvent.Message.Highlighted.Level.Five
                                "SIX" -> ChatEvent.Message.Highlighted.Level.Six
                                "SEVEN" -> ChatEvent.Message.Highlighted.Level.Seven
                                "EIGHT" -> ChatEvent.Message.Highlighted.Level.Eight
                                "NINE" -> ChatEvent.Message.Highlighted.Level.Nine
                                "TEN" -> ChatEvent.Message.Highlighted.Level.Ten
                                else -> ChatEvent.Message.Highlighted.Level.Base
                            },
                        )
                    }

                    else -> {
                        null
                    }
                }

                if (metadata != null) {
                    ChatEvent.Message.Highlighted(
                        timestamp = timestamp,
                        body = map(),
                        metadata = metadata,
                    )
                } else {
                    ChatEvent.Message.Simple(
                        body = map(),
                        timestamp = timestamp,
                    )
                }
            }
        }
    }

    private fun parseSubscriptionTier(planId: String): String {
        return when (planId) {
            SUB_T1 -> context.getString(R.string.chat_sub_tier1)
            SUB_T2 -> context.getString(R.string.chat_sub_tier2)
            SUB_T3 -> context.getString(R.string.chat_sub_tier3)
            SUB_PRIME -> context.getString(R.string.chat_sub_prime)
            else -> planId
        }
    }

    private fun parseSubscriptionTierWithArticle(planId: String): String {
        return when (planId) {
            SUB_T1 -> context.getString(R.string.chat_subGift_tier1)
            SUB_T2 -> context.getString(R.string.chat_subGift_tier2)
            SUB_T3 -> context.getString(R.string.chat_subGift_tier3)
            else -> planId
        }
    }

    private fun IrcEvent.Message.ChatMessage.map(): ChatEvent.Message.Body {
        return ChatEvent.Message.Body(
            message = message.orEmpty(),
            messageId = id,
            chatter = Chatter(
                id = userId,
                displayName = userName,
                login = userLogin,
            ),
            isAction = isAction,
            color = color,
            embeddedEmotes = embeddedEmotes.orEmpty().toImmutableList(),
            badges = badges.orEmpty().toImmutableList(),
            inReplyTo = inReplyTo?.let {
                ChatEvent.Message.Body.InReplyTo(
                    id = inReplyTo.id,
                    message = inReplyTo.message,
                    chatter = Chatter(
                        id = inReplyTo.id,
                        login = inReplyTo.userLogin,
                        displayName = inReplyTo.userName,
                    ),
                )
            },
        )
    }

    fun mapOptional(command: IrcEvent): ChatEvent? {
        return when (command) {
            is IrcEvent.Command.ClearChat -> {
                if (command.targetUserLogin != null) {
                    if (command.duration == null) {
                        ChatEvent.Message.Highlighted(
                            timestamp = command.timestamp,
                            metadata = ChatEvent.Message.Highlighted.Metadata(
                                title = command.targetUserLogin,
                                titleIcon = Icons.Default.Gavel,
                                subtitle = context.getString(R.string.chat_ban),
                            ),
                            body = null,
                        )
                    } else {
                        ChatEvent.Message.Highlighted(
                            timestamp = command.timestamp,
                            metadata = ChatEvent.Message.Highlighted.Metadata(
                                title = command.targetUserLogin,
                                titleIcon = Icons.Default.Gavel,
                                subtitle = context.getString(
                                    R.string.chat_timeout,
                                    command.duration.format(context),
                                ),
                            ),
                            body = null,
                        )
                    }
                } else {
                    ChatEvent.Message.Notice(
                        timestamp = command.timestamp,
                        text = context.getString(R.string.chat_clear),
                    )
                }
            }

            else -> null
        }
    }

    private fun getLabelForNotice(messageId: String?, message: String?): String? {
        return when (messageId) {
            "already_banned" -> context.getString(
                R.string.irc_notice_already_banned,
                message?.substringBefore(" is already banned", "") ?: "",
            )

            "already_emote_only_off" -> context.getString(R.string.irc_notice_already_emote_only_off)
            "already_emote_only_on" -> context.getString(R.string.irc_notice_already_emote_only_on)
            "already_followers_off" -> context.getString(R.string.irc_notice_already_followers_off)
            "already_followers_on" -> context.getString(
                R.string.irc_notice_already_followers_on,
                message?.substringAfter("is already in ", "")
                    ?.substringBefore(" followers-only mode", "") ?: "",
            )

            "already_r9k_off" -> context.getString(R.string.irc_notice_already_r9k_off)
            "already_r9k_on" -> context.getString(R.string.irc_notice_already_r9k_on)
            "already_slow_off" -> context.getString(R.string.irc_notice_already_slow_off)
            "already_slow_on" -> context.getString(
                R.string.irc_notice_already_slow_on,
                message?.substringAfter("is already in ", "")?.substringBefore("-second slow", "")
                    ?: "",
            )

            "already_subs_off" -> context.getString(R.string.irc_notice_already_subs_off)
            "already_subs_on" -> context.getString(R.string.irc_notice_already_subs_on)
            "autohost_receive" -> context.getString(
                R.string.irc_notice_autohost_receive,
                message?.substringBefore(" is now auto hosting", "") ?: "",
                message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "")
                    ?: "",
            )

            "bad_ban_admin" -> context.getString(
                R.string.irc_notice_bad_ban_admin,
                message?.substringAfter("cannot ban admin", "")
                    ?.substringBefore(". Please email", "") ?: "",
            )

            "bad_ban_anon" -> context.getString(R.string.irc_notice_bad_ban_anon)
            "bad_ban_broadcaster" -> context.getString(R.string.irc_notice_bad_ban_broadcaster)
            "bad_ban_mod" -> context.getString(
                R.string.irc_notice_bad_ban_mod,
                message?.substringAfter("cannot ban moderator", "")
                    ?.substringBefore(" unless you are", "") ?: "",
            )

            "bad_ban_self" -> context.getString(R.string.irc_notice_bad_ban_self)
            "bad_ban_staff" -> context.getString(
                R.string.irc_notice_bad_ban_staff,
                message?.substringAfter("cannot ban staff", "")
                    ?.substringBefore(". Please email", "") ?: "",
            )

            "bad_commercial_error" -> context.getString(R.string.irc_notice_bad_commercial_error)
            "bad_delete_message_broadcaster" -> context.getString(R.string.irc_notice_bad_delete_message_broadcaster)
            "bad_delete_message_mod" -> context.getString(
                R.string.irc_notice_bad_delete_message_mod,
                message?.substringAfter("from another moderator ", "")?.substringBeforeLast(".", "")
                    ?: "",
            )

            "bad_host_error" -> context.getString(
                R.string.irc_notice_bad_host_error,
                message?.substringAfter("a problem hosting ", "")
                    ?.substringBefore(". Please try", "") ?: "",
            )

            "bad_host_hosting" -> context.getString(
                R.string.irc_notice_bad_host_hosting,
                message?.substringAfter("is already hosting ", "")?.substringBeforeLast(".", "")
                    ?: "",
            )

            "bad_host_rate_exceeded" -> context.getString(
                R.string.irc_notice_bad_host_rate_exceeded,
                message?.substringAfter("changed more than ", "")
                    ?.substringBefore(" times every half", "") ?: "",
            )

            "bad_host_rejected" -> context.getString(R.string.irc_notice_bad_host_rejected)
            "bad_host_self" -> context.getString(R.string.irc_notice_bad_host_self)
            "bad_mod_banned" -> context.getString(
                R.string.irc_notice_bad_mod_banned,
                message?.substringBefore(" is banned", "") ?: "",
            )

            "bad_mod_mod" -> context.getString(
                R.string.irc_notice_bad_mod_mod,
                message?.substringBefore(" is already", "") ?: "",
            )

            "bad_slow_duration" -> context.getString(
                R.string.irc_notice_bad_slow_duration,
                message?.substringAfter("to more than ", "")?.substringBefore(" seconds.", "")
                    ?: "",
            )

            "bad_timeout_admin" -> context.getString(
                R.string.irc_notice_bad_timeout_admin,
                message?.substringAfter("cannot timeout admin ", "")
                    ?.substringBefore(". Please email", "") ?: "",
            )

            "bad_timeout_anon" -> context.getString(R.string.irc_notice_bad_timeout_anon)
            "bad_timeout_broadcaster" -> context.getString(R.string.irc_notice_bad_timeout_broadcaster)
            "bad_timeout_duration" -> context.getString(
                R.string.irc_notice_bad_timeout_duration,
                message?.substringAfter("for more than ", "")?.substringBeforeLast(".", "") ?: "",
            )

            "bad_timeout_mod" -> context.getString(
                R.string.irc_notice_bad_timeout_mod,
                message?.substringAfter("cannot timeout moderator ", "")
                    ?.substringBefore(" unless you are", "") ?: "",
            )

            "bad_timeout_self" -> context.getString(R.string.irc_notice_bad_timeout_self)
            "bad_timeout_staff" -> context.getString(
                R.string.irc_notice_bad_timeout_staff,
                message?.substringAfter("cannot timeout staff ", "")
                    ?.substringBefore(". Please email", "") ?: "",
            )

            "bad_unban_no_ban" -> context.getString(
                R.string.irc_notice_bad_unban_no_ban,
                message?.substringBefore(" is not banned", "") ?: "",
            )

            "bad_unhost_error" -> context.getString(R.string.irc_notice_bad_unhost_error)
            "bad_unmod_mod" -> context.getString(
                R.string.irc_notice_bad_unmod_mod,
                message?.substringBefore(" is not a", "") ?: "",
            )

            "bad_vip_grantee_banned" -> context.getString(
                R.string.irc_notice_bad_vip_grantee_banned,
                message?.substringBefore(" is banned in", "") ?: "",
            )

            "bad_vip_grantee_already_vip" -> context.getString(
                R.string.irc_notice_bad_vip_grantee_already_vip,
                message?.substringBefore(" is already a", "") ?: "",
            )

            "bad_vip_max_vips_reached" -> context.getString(R.string.irc_notice_bad_vip_max_vips_reached)
            "bad_vip_achievement_incomplete" -> context.getString(R.string.irc_notice_bad_vip_achievement_incomplete)
            "bad_unvip_grantee_not_vip" -> context.getString(
                R.string.irc_notice_bad_unvip_grantee_not_vip,
                message?.substringBefore(" is not a", "") ?: "",
            )

            "ban_success" -> context.getString(
                R.string.irc_notice_ban_success,
                message?.substringBefore(" is now banned", "") ?: "",
            )

            "cmds_available" -> context.getString(
                R.string.irc_notice_cmds_available,
                message?.substringAfter("details): ", "")?.substringBefore(" More help:", "") ?: "",
            )

            "color_changed" -> context.getString(R.string.irc_notice_color_changed)
            "commercial_success" -> context.getString(
                R.string.irc_notice_commercial_success,
                message?.substringAfter("Initiating ", "")
                    ?.substringBefore(" second commercial break.", "") ?: "",
            )

            "delete_message_success" -> context.getString(
                R.string.irc_notice_delete_message_success,
                message?.substringAfter("The message from ", "")
                    ?.substringBefore(" is now deleted.", "") ?: "",
            )

            "delete_staff_message_success" -> context.getString(
                R.string.irc_notice_delete_staff_message_success,
                message?.substringAfter("message from staff ", "")
                    ?.substringBefore(". Please email", "") ?: "",
            )

            "emote_only_off" -> context.getString(R.string.irc_notice_emote_only_off)
            "emote_only_on" -> context.getString(R.string.irc_notice_emote_only_on)
            "followers_off" -> context.getString(R.string.irc_notice_followers_off)
            "followers_on" -> context.getString(
                R.string.irc_notice_followers_on,
                message?.substringAfter("is now in ", "")
                    ?.substringBefore(" followers-only mode", "") ?: "",
            )

            "followers_on_zero" -> context.getString(R.string.irc_notice_followers_on_zero)
            "host_off" -> context.getString(R.string.irc_notice_host_off)
            "host_on" -> context.getString(
                R.string.irc_notice_host_on,
                message?.substringAfter("Now hosting ", "")?.substringBeforeLast(".", "") ?: "",
            )

            "host_receive" -> context.getString(
                R.string.irc_notice_host_receive,
                message?.substringBefore(" is now hosting", "") ?: "",
                message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "")
                    ?: "",
            )

            "host_receive_no_count" -> context.getString(
                R.string.irc_notice_host_receive_no_count,
                message?.substringBefore(" is now hosting", "") ?: "",
            )

            "host_target_went_offline" -> context.getString(
                R.string.irc_notice_host_target_went_offline,
                message?.substringBefore(" has gone offline", "") ?: "",
            )

            "hosts_remaining" -> context.getString(
                R.string.irc_notice_hosts_remaining,
                message?.substringBefore(" host commands", "") ?: "",
            )

            "invalid_user" -> context.getString(
                R.string.irc_notice_invalid_user,
                message?.substringAfter("Invalid username: ", "") ?: "",
            )

            "mod_success" -> context.getString(
                R.string.irc_notice_mod_success,
                message?.substringAfter("You have added ", "")
                    ?.substringBefore(" as a moderator", "") ?: "",
            )

            "msg_banned" -> context.getString(
                R.string.irc_notice_msg_banned,
                message?.substringAfter("from talking in ", "")?.substringBeforeLast(".", "") ?: "",
            )

            "msg_bad_characters" -> context.getString(R.string.irc_notice_msg_bad_characters)
            "msg_channel_blocked" -> context.getString(R.string.irc_notice_msg_channel_blocked)
            "msg_channel_suspended" -> context.getString(R.string.irc_notice_msg_channel_suspended)
            "msg_duplicate" -> context.getString(R.string.irc_notice_msg_duplicate)
            "msg_emoteonly" -> context.getString(R.string.irc_notice_msg_emoteonly)
            "msg_followersonly" -> context.getString(
                R.string.irc_notice_msg_followersonly,
                message?.substringAfter("This room is in ", "")
                    ?.substringBefore(" followers-only mode", "") ?: "",
                message?.substringAfter("Follow ", "")?.substringBefore(" to join", "") ?: "",
            )

            "msg_followersonly_followed" -> context.getString(
                R.string.irc_notice_msg_followersonly_followed,
                message?.substringAfter("This room is in ", "")
                    ?.substringBefore(" followers-only mode", "") ?: "",
                message?.substringAfter("following for ", "")?.substringBefore(". Continue", "")
                    ?: "",
            )

            "msg_followersonly_zero" -> context.getString(
                R.string.irc_notice_msg_followersonly_zero,
                message?.substringAfter(". Follow ", "")?.substringBefore(" to join the", "") ?: "",
            )

            "msg_r9k" -> context.getString(R.string.irc_notice_msg_r9k)
            "msg_ratelimit" -> context.getString(R.string.irc_notice_msg_ratelimit)
            "msg_rejected" -> context.getString(R.string.irc_notice_msg_rejected)
            "msg_rejected_mandatory" -> context.getString(R.string.irc_notice_msg_rejected_mandatory)
            "msg_slowmode" -> context.getString(
                R.string.irc_notice_msg_slowmode,
                message?.substringAfter("talk again in ", "")?.substringBefore(" seconds.", "")
                    ?: "",
            )

            "msg_subsonly" -> context.getString(
                R.string.irc_notice_msg_subsonly,
                message?.substringAfter("/products/", "")?.substringBefore("/ticket?ref", "") ?: "",
            )

            "msg_suspended" -> context.getString(R.string.irc_notice_msg_suspended)
            "msg_timedout" -> context.getString(
                R.string.irc_notice_msg_timedout,
                message?.substringAfter("timed out for ", "")?.substringBefore(" more seconds.", "")
                    ?: "",
            )

            "msg_verified_email" -> context.getString(R.string.irc_notice_msg_verified_email)
            "no_help" -> context.getString(R.string.irc_notice_no_help)
            "no_mods" -> context.getString(R.string.irc_notice_no_mods)
            "no_vips" -> context.getString(R.string.irc_notice_no_vips)
            "not_hosting" -> context.getString(R.string.irc_notice_not_hosting)
            "no_permission" -> context.getString(R.string.irc_notice_no_permission)
            "r9k_off" -> context.getString(R.string.irc_notice_r9k_off)
            "r9k_on" -> context.getString(R.string.irc_notice_r9k_on)
            "raid_error_already_raiding" -> context.getString(R.string.irc_notice_raid_error_already_raiding)
            "raid_error_forbidden" -> context.getString(R.string.irc_notice_raid_error_forbidden)
            "raid_error_self" -> context.getString(R.string.irc_notice_raid_error_self)
            "raid_error_too_many_viewers" -> context.getString(R.string.irc_notice_raid_error_too_many_viewers)
            "raid_error_unexpected" -> context.getString(
                R.string.irc_notice_raid_error_unexpected,
                message?.substringAfter("a problem raiding ", "")
                    ?.substringBefore(". Please try", "") ?: "",
            )

            "raid_notice_mature" -> context.getString(R.string.irc_notice_raid_notice_mature)
            "raid_notice_restricted_chat" -> context.getString(R.string.irc_notice_raid_notice_restricted_chat)
            "room_mods" -> context.getString(
                R.string.irc_notice_room_mods,
                message?.substringAfter("this channel are: ", "") ?: "",
            )

            "slow_off" -> context.getString(R.string.irc_notice_slow_off)
            "slow_on" -> context.getString(
                R.string.irc_notice_slow_on,
                message?.substringAfter("send messages every ", "")
                    ?.substringBefore(" seconds.", "") ?: "",
            )

            "subs_off" -> context.getString(R.string.irc_notice_subs_off)
            "subs_on" -> context.getString(R.string.irc_notice_subs_on)
            "timeout_no_timeout" -> context.getString(
                R.string.irc_notice_timeout_no_timeout,
                message?.substringBefore(" is not timed", "") ?: "",
            )

            "timeout_success" -> context.getString(
                R.string.irc_notice_timeout_success,
                message?.substringBefore(" has been", "") ?: "",
                message?.substringAfter("timed out for ", "")?.substringBeforeLast(".", "") ?: "",
            )

            "tos_ban" -> context.getString(
                R.string.irc_notice_tos_ban,
                message?.substringAfter("has closed channel ", "")
                    ?.substringBefore(" due to Terms", "") ?: "",
            )

            "turbo_only_color" -> context.getString(
                R.string.irc_notice_turbo_only_color,
                message?.substringAfter("following instead: ", "") ?: "",
            )

            "unavailable_command" -> context.getString(
                R.string.irc_notice_unavailable_command,
                message?.substringAfter("Sorry, “", "")?.substringBefore("” is not available", "")
                    ?: "",
            )

            "unban_success" -> context.getString(
                R.string.irc_notice_unban_success,
                message?.substringBefore(" is no longer", "") ?: "",
            )

            "unmod_success" -> context.getString(
                R.string.irc_notice_unmod_success,
                message?.substringAfter("You have removed ", "")
                    ?.substringBefore(" as a moderator", "") ?: "",
            )

            "unraid_error_no_active_raid" -> context.getString(R.string.irc_notice_unraid_error_no_active_raid)
            "unraid_error_unexpected" -> context.getString(R.string.irc_notice_unraid_error_unexpected)
            "unraid_success" -> context.getString(R.string.irc_notice_unraid_success)
            "unrecognized_cmd" -> context.getString(
                R.string.irc_notice_unrecognized_cmd,
                message?.substringAfter("Unrecognized command: ", "") ?: "",
            )

            "untimeout_banned" -> context.getString(
                R.string.irc_notice_untimeout_banned,
                message?.substringBefore(" is permanently banned", "") ?: "",
            )

            "untimeout_success" -> context.getString(
                R.string.irc_notice_untimeout_success,
                message?.substringBefore(" is no longer", "") ?: "",
            )

            "unvip_success" -> context.getString(
                R.string.irc_notice_unvip_success,
                message?.substringAfter("You have removed ", "")?.substringBefore(" as a VIP", "")
                    ?: "",
            )

            "usage_ban" -> context.getString(R.string.irc_notice_usage_ban)
            "usage_clear" -> context.getString(R.string.irc_notice_usage_clear)
            "usage_color" -> context.getString(
                R.string.irc_notice_usage_color,
                message?.substringAfter("following: ", "")?.substringBeforeLast(".", "") ?: "",
            )

            "usage_commercial" -> context.getString(R.string.irc_notice_usage_commercial)
            "usage_disconnect" -> context.getString(R.string.irc_notice_usage_disconnect)
            "usage_delete" -> context.getString(R.string.irc_notice_usage_delete)
            "usage_emote_only_off" -> context.getString(R.string.irc_notice_usage_emote_only_off)
            "usage_emote_only_on" -> context.getString(R.string.irc_notice_usage_emote_only_on)
            "usage_followers_off" -> context.getString(R.string.irc_notice_usage_followers_off)
            "usage_followers_on" -> context.getString(R.string.irc_notice_usage_followers_on)
            "usage_help" -> context.getString(R.string.irc_notice_usage_help)
            "usage_host" -> context.getString(R.string.irc_notice_usage_host)
            "usage_marker" -> context.getString(R.string.irc_notice_usage_marker)
            "usage_me" -> context.getString(R.string.irc_notice_usage_me)
            "usage_mod" -> context.getString(R.string.irc_notice_usage_mod)
            "usage_mods" -> context.getString(R.string.irc_notice_usage_mods)
            "usage_r9k_off" -> context.getString(R.string.irc_notice_usage_r9k_off)
            "usage_r9k_on" -> context.getString(R.string.irc_notice_usage_r9k_on)
            "usage_raid" -> context.getString(R.string.irc_notice_usage_raid)
            "usage_slow_off" -> context.getString(R.string.irc_notice_usage_slow_off)
            "usage_slow_on" -> context.getString(
                R.string.irc_notice_usage_slow_on,
                message?.substringAfter("default=", "")?.substringBefore(")", "") ?: "",
            )

            "usage_subs_off" -> context.getString(R.string.irc_notice_usage_subs_off)
            "usage_subs_on" -> context.getString(R.string.irc_notice_usage_subs_on)
            "usage_timeout" -> context.getString(R.string.irc_notice_usage_timeout)
            "usage_unban" -> context.getString(R.string.irc_notice_usage_unban)
            "usage_unhost" -> context.getString(R.string.irc_notice_usage_unhost)
            "usage_unmod" -> context.getString(R.string.irc_notice_usage_unmod)
            "usage_unraid" -> context.getString(R.string.irc_notice_usage_unraid)
            "usage_untimeout" -> context.getString(R.string.irc_notice_usage_untimeout)
            "usage_unvip" -> context.getString(R.string.irc_notice_usage_unvip)
            "usage_user" -> context.getString(R.string.irc_notice_usage_user)
            "usage_vip" -> context.getString(R.string.irc_notice_usage_vip)
            "usage_vips" -> context.getString(R.string.irc_notice_usage_vips)
            "usage_whisper" -> context.getString(R.string.irc_notice_usage_whisper)
            "vip_success" -> context.getString(
                R.string.irc_notice_vip_success,
                message?.substringAfter("You have added ", "")?.substringBeforeLast(" as a vip", "")
                    ?: "",
            )

            "vips_success" -> context.getString(
                R.string.irc_notice_vips_success,
                message?.substringAfter("channel are: ", "")?.substringBeforeLast(".", "") ?: "",
            )

            "whisper_banned" -> context.getString(R.string.irc_notice_whisper_banned)
            "whisper_banned_recipient" -> context.getString(R.string.irc_notice_whisper_banned_recipient)
            "whisper_invalid_login" -> context.getString(R.string.irc_notice_whisper_invalid_login)
            "whisper_invalid_self" -> context.getString(R.string.irc_notice_whisper_invalid_self)
            "whisper_limit_per_min" -> context.getString(R.string.irc_notice_whisper_limit_per_min)
            "whisper_limit_per_sec" -> context.getString(R.string.irc_notice_whisper_limit_per_sec)
            "whisper_restricted" -> context.getString(R.string.irc_notice_whisper_restricted)
            "whisper_restricted_recipient" -> context.getString(R.string.irc_notice_whisper_restricted_recipient)
            else -> null
        }
    }

    private companion object {
        const val SUB_T1 = "1000"
        const val SUB_T2 = "2000"
        const val SUB_T3 = "3000"
        const val SUB_PRIME = "Prime"
    }
}