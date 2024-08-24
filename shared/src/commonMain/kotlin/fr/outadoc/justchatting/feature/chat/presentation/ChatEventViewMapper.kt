package fr.outadoc.justchatting.feature.chat.presentation

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Icon
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.formatCurrency
import fr.outadoc.justchatting.utils.presentation.formatNumber
import io.fluidsonic.currency.Currency
import kotlinx.collections.immutable.toImmutableList

internal class ChatEventViewMapper {

    fun map(command: ChatEvent): List<ChatListItem> {
        return when (command) {
            is ChatEvent.Message -> {
                listOf(mapMessage(command))
            }

            is ChatEvent.Command.UserState -> {
                listOf(
                    ChatListItem.UserState(
                        emoteSets = command.emoteSets.toImmutableList(),
                    ),
                )
            }

            is ChatEvent.Command.RoomStateDelta -> {
                listOf(
                    ChatListItem.RoomStateDelta(
                        isEmoteOnly = command.isEmoteOnly,
                        minFollowDuration = command.minFollowDuration,
                        uniqueMessagesOnly = command.uniqueMessagesOnly,
                        slowModeDuration = command.slowModeDuration,
                        isSubOnly = command.isSubOnly,
                    ),
                )
            }

            is ChatEvent.Command.ClearChat -> {
                listOf(
                    ChatListItem.RemoveContent(
                        upUntil = command.timestamp,
                        matchingUserId = command.targetUserId,
                    ),
                    if (command.targetUserLogin != null) {
                        if (command.duration == null) {
                            ChatListItem.Message.Highlighted(
                                timestamp = command.timestamp,
                                metadata = ChatListItem.Message.Highlighted.Metadata(
                                    title = command.targetUserLogin.desc(),
                                    titleIcon = Icon.Gavel,
                                    subtitle = MR.strings.chat_ban.desc(),
                                ),
                                body = null,
                            )
                        } else {
                            ChatListItem.Message.Highlighted(
                                timestamp = command.timestamp,
                                metadata = ChatListItem.Message.Highlighted.Metadata(
                                    title = command.targetUserLogin.desc(),
                                    titleIcon = Icon.Gavel,
                                    subtitle = MR.strings.chat_timeout.format(command.duration),
                                ),
                                body = null,
                            )
                        }
                    } else {
                        ChatListItem.Message.Notice(
                            timestamp = command.timestamp,
                            text = MR.strings.chat_clear.desc(),
                        )
                    },
                )
            }

            is ChatEvent.Command.ClearMessage -> {
                listOf(
                    ChatListItem.RemoveContent(
                        upUntil = command.timestamp,
                        matchingMessageId = command.targetMessageId,
                    ),
                )
            }

            ChatEvent.Command.Ping -> {
                emptyList()
            }
        }
    }

    private fun mapMessage(chatEvent: ChatEvent.Message): ChatListItem = with(chatEvent) {
        when (this) {
            is ChatEvent.Message.Notice -> {
                ChatListItem.Message.Notice(
                    timestamp = timestamp,
                    text = getLabelForNotice(messageId = messageId, message = message)
                        ?: message.desc(),
                )
            }

            is ChatEvent.Message.IncomingRaid -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.CallReceived,
                        subtitle = MR.strings.chat_raid_header
                            .format(
                                MR.plurals.viewers.format(
                                    number = raidersCount,
                                    raidersCount,
                                ),
                            ),
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.CancelledRaid -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.Cancel,
                        subtitle = MR.strings.chat_unraid_subtitle.desc(),
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.HighlightedMessage -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = MR.strings.irc_msgid_highlighted_message.desc(),
                        titleIcon = Icon.Highlight,
                        subtitle = null,
                    ),
                    body = userMessage.map(),
                )
            }

            is ChatEvent.Message.Announcement -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = MR.strings.irc_msgid_announcement.desc(),
                        titleIcon = Icon.Campaign,
                        subtitle = null,
                    ),
                    body = userMessage.map(),
                )
            }

            is ChatEvent.Message.Subscription -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = when (subscriptionPlan) {
                            SUB_PRIME -> Icon.Star
                            else -> Icon.Star
                        },
                        subtitle = when (streakMonths) {
                            0 -> {
                                MR.strings.chat_sub_header_withDuration
                                    .format(
                                        parseSubscriptionTier(subscriptionPlan),
                                        MR.plurals.months.format(
                                            number = cumulativeMonths,
                                            cumulativeMonths.formatNumber(),
                                        ),
                                    )
                            }

                            else -> {
                                MR.strings.chat_sub_header_withDurationAndStreak.format(
                                    parseSubscriptionTier(subscriptionPlan),
                                    MR.plurals.months.format(
                                        number = cumulativeMonths,
                                        cumulativeMonths.formatNumber(),
                                    ),
                                    MR.plurals.months.format(
                                        number = streakMonths,
                                        streakMonths.formatNumber(),
                                    ),
                                )
                            }
                        },
                    ),
                    body = userMessage?.map(),
                )
            }

            is ChatEvent.Message.SubscriptionConversion -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.Star,
                        subtitle = MR.strings.chat_subConversion_header
                            .format(parseSubscriptionTierWithArticle(subscriptionPlan)),
                    ),
                    body = userMessage?.map(),
                )
            }

            is ChatEvent.Message.MassSubscriptionGift -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.VolunteerActivism,
                        subtitle = MR.strings.chat_massSubGift_header
                            .format(
                                giftCount.formatNumber(),
                                parseSubscriptionTierWithArticle(subscriptionPlan),
                                totalChannelGiftCount.formatNumber(),
                            ),
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.SubscriptionGift -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    body = null,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.Redeem,
                        subtitle = MR.strings.chat_subGift_header
                            .format(
                                parseSubscriptionTier(subscriptionPlan),
                                recipientDisplayName,
                                MR.plurals.months.format(
                                    number = cumulativeMonths,
                                    cumulativeMonths.formatNumber(),
                                ),
                            ),
                    ),
                )
            }

            is ChatEvent.Message.GiftPayForward -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.FastForward,
                        subtitle = when (priorGifterDisplayName) {
                            null -> {
                                MR.strings.chat_subGift_payForwardAnonymous.desc()
                            }

                            else -> {
                                MR.strings.chat_subGift_payForward.format(priorGifterDisplayName)
                            }
                        },
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.UserNotice -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    body = userMessage?.map(),
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = systemMsg.desc(),
                        titleIcon = null,
                        subtitle = null,
                    ),
                )
            }

            is ChatEvent.Message.Join -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = MR.strings.chat_join.format(channelLogin),
                        subtitle = null,
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.SendError -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = MR.strings.chat_send_msg_error.desc(),
                        subtitle = null,
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.ChatMessage -> {
                val metadata = when {
                    isFirstMessageByUser -> {
                        ChatListItem.Message.Highlighted.Metadata(
                            title = MR.strings.chat_first.desc(),
                            titleIcon = Icon.WavingHand,
                            subtitle = null,
                        )
                    }

                    rewardId != null -> {
                        ChatListItem.Message.Highlighted.Metadata(
                            title = MR.strings.chat_reward.desc(),
                            titleIcon = Icon.Toll,
                            subtitle = null,
                        )
                    }

                    paidMessageInfo != null -> {
                        val currency = Currency.forCode(paidMessageInfo.currency)
                        val amount = BigDecimal.fromLongWithExponent(
                            paidMessageInfo.amount,
                            paidMessageInfo.exponent,
                        )

                        val formattedAmount = amount.formatCurrency(currency)
                        val header = MR.strings.chat_paidMessage.format(formattedAmount)

                        ChatListItem.Message.Highlighted.Metadata(
                            title = header,
                            titleIcon = Icon.Bolt,
                            subtitle = null,
                            level = when (paidMessageInfo.level) {
                                "ONE" -> ChatListItem.Message.Highlighted.Level.One
                                "TWO" -> ChatListItem.Message.Highlighted.Level.Two
                                "THREE" -> ChatListItem.Message.Highlighted.Level.Three
                                "FOUR" -> ChatListItem.Message.Highlighted.Level.Four
                                "FIVE" -> ChatListItem.Message.Highlighted.Level.Five
                                "SIX" -> ChatListItem.Message.Highlighted.Level.Six
                                "SEVEN" -> ChatListItem.Message.Highlighted.Level.Seven
                                "EIGHT" -> ChatListItem.Message.Highlighted.Level.Eight
                                "NINE" -> ChatListItem.Message.Highlighted.Level.Nine
                                "TEN" -> ChatListItem.Message.Highlighted.Level.Ten
                                else -> ChatListItem.Message.Highlighted.Level.Base
                            },
                        )
                    }

                    else -> {
                        null
                    }
                }

                if (metadata != null) {
                    ChatListItem.Message.Highlighted(
                        timestamp = timestamp,
                        body = map(),
                        metadata = metadata,
                    )
                } else {
                    ChatListItem.Message.Simple(
                        body = map(),
                        timestamp = timestamp,
                    )
                }
            }

            is ChatEvent.Message.BroadcastSettingsUpdate -> {
                ChatListItem.BroadcastSettingsUpdate(
                    streamTitle = streamTitle,
                    streamCategory = StreamCategory(
                        id = categoryId,
                        name = categoryName,
                    ),
                )
            }

            is ChatEvent.Message.PinnedMessageUpdate -> {
                ChatListItem.PinnedMessageUpdate(
                    pinnedMessage = pinnedMessage,
                )
            }

            is ChatEvent.Message.PollUpdate -> {
                ChatListItem.PollUpdate(
                    poll = poll,
                )
            }

            is ChatEvent.Message.PredictionUpdate -> {
                ChatListItem.PredictionUpdate(
                    prediction = prediction,
                )
            }

            is ChatEvent.Message.RaidUpdate -> {
                ChatListItem.RaidUpdate(
                    raid = raid,
                )
            }

            is ChatEvent.Message.RedemptionUpdate -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = MR.plurals.user_redeemed
                            .format(
                                number = redemption.reward.cost,
                                redemption.userDisplayName,
                                redemption.reward.title,
                                redemption.reward.cost,
                            ),
                        titleIcon = Icon.Toll,
                        subtitle = null,
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.RichEmbed -> {
                ChatListItem.RichEmbed(
                    messageId = messageId,
                    title = title,
                    requestUrl = requestUrl,
                    thumbnailUrl = thumbnailUrl,
                    authorName = authorName,
                    channelName = channelName,
                )
            }

            is ChatEvent.Message.ViewerCountUpdate -> {
                ChatListItem.ViewerCountUpdate(
                    viewerCount = viewerCount,
                )
            }
        }
    }

    private fun parseSubscriptionTier(planId: String): StringDesc {
        return when (planId) {
            SUB_T1 -> MR.strings.chat_sub_tier1.desc()
            SUB_T2 -> MR.strings.chat_sub_tier2.desc()
            SUB_T3 -> MR.strings.chat_sub_tier3.desc()
            SUB_PRIME -> MR.strings.chat_sub_prime.desc()
            else -> planId.desc()
        }
    }

    private fun parseSubscriptionTierWithArticle(planId: String): StringDesc {
        return when (planId) {
            SUB_T1 -> MR.strings.chat_subGift_tier1.desc()
            SUB_T2 -> MR.strings.chat_subGift_tier2.desc()
            SUB_T3 -> MR.strings.chat_subGift_tier3.desc()
            else -> planId.desc()
        }
    }

    private fun ChatEvent.Message.ChatMessage.map(): ChatListItem.Message.Body {
        val mentions = message.orEmpty().getMentionsPrefix()
        val mentionsLength = mentions.sumOf { mention -> mention.length }

        return ChatListItem.Message.Body(
            message = message.orEmpty()
                .drop(mentionsLength)
                .removePrefix(" "),
            messageId = id,
            chatter = Chatter(
                id = userId,
                displayName = userName,
                login = userLogin,
            ),
            isAction = isAction,
            color = color,
            embeddedEmotes = embeddedEmotes.toImmutableList(),
            badges = badges.orEmpty().toImmutableList(),
            inReplyTo = if (mentions.isNotEmpty()) {
                ChatListItem.Message.Body.InReplyTo(
                    message = inReplyTo?.message,
                    mentions = mentions.map { mention ->
                        mention.drop(1)
                    },
                )
            } else {
                null
            },
        )
    }

    private fun getLabelForNotice(messageId: String?, message: String?): StringDesc? {
        return when (messageId) {
            "already_banned" -> {
                MR.strings.irc_notice_already_banned
                    .format(
                        message?.substringBefore(" is already banned", "") ?: "",
                    )
            }

            "already_emote_only_off" -> {
                MR.strings.irc_notice_already_emote_only_off.desc()
            }

            "already_emote_only_on" -> {
                MR.strings.irc_notice_already_emote_only_on.desc()
            }

            "already_followers_off" -> {
                MR.strings.irc_notice_already_followers_off.desc()
            }

            "already_followers_on" -> {
                MR.strings.irc_notice_already_followers_on.format(
                    message?.substringAfter("is already in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                )
            }

            "already_r9k_off" -> {
                MR.strings.irc_notice_already_r9k_off.desc()
            }

            "already_r9k_on" -> {
                MR.strings.irc_notice_already_r9k_on.desc()
            }

            "already_slow_off" -> {
                MR.strings.irc_notice_already_slow_off.desc()
            }

            "already_slow_on" -> {
                MR.strings.irc_notice_already_slow_on.format(
                    message?.substringAfter("is already in ", "")
                        ?.substringBefore("-second slow", "")
                        ?: "",
                )
            }

            "already_subs_off" -> {
                MR.strings.irc_notice_already_subs_off.desc()
            }

            "already_subs_on" -> {
                MR.strings.irc_notice_already_subs_on.desc()
            }

            "autohost_receive" -> {
                MR.strings.irc_notice_autohost_receive.format(
                    message?.substringBefore(" is now auto hosting", "") ?: "",
                    message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "")
                        ?: "",
                )
            }

            "bad_ban_admin" -> {
                MR.strings.irc_notice_bad_ban_admin.format(
                    message?.substringAfter("cannot ban admin", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_ban_anon" -> {
                MR.strings.irc_notice_bad_ban_anon.desc()
            }

            "bad_ban_broadcaster" -> {
                MR.strings.irc_notice_bad_ban_broadcaster.desc()
            }

            "bad_ban_mod" -> {
                MR.strings.irc_notice_bad_ban_mod.format(
                    message?.substringAfter("cannot ban moderator", "")
                        ?.substringBefore(" unless you are", "") ?: "",
                )
            }

            "bad_ban_self" -> {
                MR.strings.irc_notice_bad_ban_self.desc()
            }

            "bad_ban_staff" -> {
                MR.strings.irc_notice_bad_ban_staff.format(
                    message?.substringAfter("cannot ban staff", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_commercial_error" -> {
                MR.strings.irc_notice_bad_commercial_error.desc()
            }

            "bad_delete_message_broadcaster" -> {
                MR.strings.irc_notice_bad_delete_message_broadcaster.desc()
            }

            "bad_delete_message_mod" -> {
                MR.strings.irc_notice_bad_delete_message_mod.format(
                    message?.substringAfter("from another moderator ", "")
                        ?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "bad_host_error" -> {
                MR.strings.irc_notice_bad_host_error.format(
                    message?.substringAfter("a problem hosting ", "")
                        ?.substringBefore(". Please try", "") ?: "",
                )
            }

            "bad_host_hosting" -> {
                MR.strings.irc_notice_bad_host_hosting.format(
                    message?.substringAfter("is already hosting ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "bad_host_rate_exceeded" -> {
                MR.strings.irc_notice_bad_host_rate_exceeded.format(
                    message?.substringAfter("changed more than ", "")
                        ?.substringBefore(" times every half", "") ?: "",
                )
            }

            "bad_host_rejected" -> {
                MR.strings.irc_notice_bad_host_rejected.desc()
            }

            "bad_host_self" -> {
                MR.strings.irc_notice_bad_host_self.desc()
            }

            "bad_mod_banned" -> {
                MR.strings.irc_notice_bad_mod_banned.format(
                    message?.substringBefore(" is banned", "") ?: "",
                )
            }

            "bad_mod_mod" -> {
                MR.strings.irc_notice_bad_mod_mod.format(
                    message?.substringBefore(" is already", "") ?: "",
                )
            }

            "bad_slow_duration" -> {
                MR.strings.irc_notice_bad_slow_duration.format(
                    message?.substringAfter("to more than ", "")?.substringBefore(" seconds.", "")
                        ?: "",
                )
            }

            "bad_timeout_admin" -> {
                MR.strings.irc_notice_bad_timeout_admin.format(
                    message?.substringAfter("cannot timeout admin ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_timeout_anon" -> {
                MR.strings.irc_notice_bad_timeout_anon.desc()
            }

            "bad_timeout_broadcaster" -> {
                MR.strings.irc_notice_bad_timeout_broadcaster.desc()
            }

            "bad_timeout_duration" -> {
                MR.strings.irc_notice_bad_timeout_duration.format(
                    message?.substringAfter("for more than ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "bad_timeout_mod" -> {
                MR.strings.irc_notice_bad_timeout_mod.format(
                    message?.substringAfter("cannot timeout moderator ", "")
                        ?.substringBefore(" unless you are", "") ?: "",
                )
            }

            "bad_timeout_self" -> {
                MR.strings.irc_notice_bad_timeout_self.desc()
            }

            "bad_timeout_staff" -> {
                MR.strings.irc_notice_bad_timeout_staff.format(
                    message?.substringAfter("cannot timeout staff ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_unban_no_ban" -> {
                MR.strings.irc_notice_bad_unban_no_ban.format(
                    message?.substringBefore(" is not banned", "") ?: "",
                )
            }

            "bad_unhost_error" -> {
                MR.strings.irc_notice_bad_unhost_error.desc()
            }

            "bad_unmod_mod" -> {
                MR.strings.irc_notice_bad_unmod_mod.format(
                    message?.substringBefore(" is not a", "") ?: "",
                )
            }

            "bad_vip_grantee_banned" -> {
                MR.strings.irc_notice_bad_vip_grantee_banned.format(
                    message?.substringBefore(" is banned in", "") ?: "",
                )
            }

            "bad_vip_grantee_already_vip" -> {
                MR.strings.irc_notice_bad_vip_grantee_already_vip.format(
                    message?.substringBefore(" is already a", "") ?: "",
                )
            }

            "bad_vip_max_vips_reached" -> {
                MR.strings.irc_notice_bad_vip_max_vips_reached.desc()
            }

            "bad_vip_achievement_incomplete" -> {
                MR.strings.irc_notice_bad_vip_achievement_incomplete.desc()
            }

            "bad_unvip_grantee_not_vip" -> {
                MR.strings.irc_notice_bad_unvip_grantee_not_vip.format(
                    message?.substringBefore(" is not a", "") ?: "",
                )
            }

            "ban_success" -> {
                MR.strings.irc_notice_ban_success.format(
                    message?.substringBefore(" is now banned", "") ?: "",
                )
            }

            "cmds_available" -> {
                MR.strings.irc_notice_cmds_available.format(
                    message?.substringAfter("details): ", "")?.substringBefore(" More help:", "")
                        ?: "",
                )
            }

            "color_changed" -> {
                MR.strings.irc_notice_color_changed.desc()
            }

            "commercial_success" -> {
                MR.strings.irc_notice_commercial_success.format(
                    message?.substringAfter("Initiating ", "")
                        ?.substringBefore(" second commercial break.", "") ?: "",
                )
            }

            "delete_message_success" -> {
                MR.strings.irc_notice_delete_message_success.format(
                    message?.substringAfter("The message from ", "")
                        ?.substringBefore(" is now deleted.", "") ?: "",
                )
            }

            "delete_staff_message_success" -> {
                MR.strings.irc_notice_delete_staff_message_success.format(
                    message?.substringAfter("message from staff ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "emote_only_off" -> {
                MR.strings.irc_notice_emote_only_off.desc()
            }

            "emote_only_on" -> {
                MR.strings.irc_notice_emote_only_on.desc()
            }

            "followers_off" -> {
                MR.strings.irc_notice_followers_off.desc()
            }

            "followers_on" -> {
                MR.strings.irc_notice_followers_on.format(
                    message?.substringAfter("is now in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                )
            }

            "followers_on_zero" -> {
                MR.strings.irc_notice_followers_on_zero.desc()
            }

            "host_off" -> {
                MR.strings.irc_notice_host_off.desc()
            }

            "host_on" -> {
                MR.strings.irc_notice_host_on.format(
                    message?.substringAfter("Now hosting ", "")?.substringBeforeLast(".", "") ?: "",
                )
            }

            "host_receive" -> {
                MR.strings.irc_notice_host_receive.format(
                    message?.substringBefore(" is now hosting", "") ?: "",
                    message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "")
                        ?: "",
                )
            }

            "host_receive_no_count" -> {
                MR.strings.irc_notice_host_receive_no_count.format(
                    message?.substringBefore(" is now hosting", "") ?: "",
                )
            }

            "host_target_went_offline" -> {
                MR.strings.irc_notice_host_target_went_offline.format(
                    message?.substringBefore(" has gone offline", "") ?: "",
                )
            }

            "hosts_remaining" -> {
                MR.strings.irc_notice_hosts_remaining.format(
                    message?.substringBefore(" host commands", "") ?: "",
                )
            }

            "invalid_user" -> {
                MR.strings.irc_notice_invalid_user.format(
                    message?.substringAfter("Invalid username: ", "") ?: "",
                )
            }

            "mod_success" -> {
                MR.strings.irc_notice_mod_success.format(
                    message?.substringAfter("You have added ", "")
                        ?.substringBefore(" as a moderator", "") ?: "",
                )
            }

            "msg_banned" -> {
                MR.strings.irc_notice_msg_banned.format(
                    message?.substringAfter("from talking in ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "msg_bad_characters" -> {
                MR.strings.irc_notice_msg_bad_characters.desc()
            }

            "msg_channel_blocked" -> {
                MR.strings.irc_notice_msg_channel_blocked.desc()
            }

            "msg_channel_suspended" -> {
                MR.strings.irc_notice_msg_channel_suspended.desc()
            }

            "msg_duplicate" -> {
                MR.strings.irc_notice_msg_duplicate.desc()
            }

            "msg_emoteonly" -> {
                MR.strings.irc_notice_msg_emoteonly.desc()
            }

            "msg_followersonly" -> {
                MR.strings.irc_notice_msg_followersonly.format(
                    message?.substringAfter("This room is in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                    message?.substringAfter("Follow ", "")?.substringBefore(" to join", "") ?: "",
                )
            }

            "msg_followersonly_followed" -> {
                MR.strings.irc_notice_msg_followersonly_followed.format(
                    message?.substringAfter("This room is in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                    message?.substringAfter("following for ", "")?.substringBefore(". Continue", "")
                        ?: "",
                )
            }

            "msg_followersonly_zero" -> {
                MR.strings.irc_notice_msg_followersonly_zero.format(
                    message?.substringAfter(". Follow ", "")?.substringBefore(" to join the", "")
                        ?: "",
                )
            }

            "msg_r9k" -> {
                MR.strings.irc_notice_msg_r9k.desc()
            }

            "msg_ratelimit" -> {
                MR.strings.irc_notice_msg_ratelimit.desc()
            }

            "msg_rejected" -> {
                MR.strings.irc_notice_msg_rejected.desc()
            }

            "msg_rejected_mandatory" -> {
                MR.strings.irc_notice_msg_rejected_mandatory.desc()
            }

            "msg_slowmode" -> {
                MR.strings.irc_notice_msg_slowmode.format(
                    message?.substringAfter("talk again in ", "")?.substringBefore(" seconds.", "")
                        ?: "",
                )
            }

            "msg_subsonly" -> {
                MR.strings.irc_notice_msg_subsonly.format(
                    message?.substringAfter("/products/", "")?.substringBefore("/ticket?ref", "")
                        ?: "",
                )
            }

            "msg_suspended" -> {
                MR.strings.irc_notice_msg_suspended.desc()
            }

            "msg_timedout" -> {
                MR.strings.irc_notice_msg_timedout.format(
                    message?.substringAfter("timed out for ", "")
                        ?.substringBefore(" more seconds.", "")
                        ?: "",
                )
            }

            "msg_verified_email" -> {
                MR.strings.irc_notice_msg_verified_email.desc()
            }

            "no_help" -> {
                MR.strings.irc_notice_no_help.desc()
            }

            "no_mods" -> {
                MR.strings.irc_notice_no_mods.desc()
            }

            "no_vips" -> {
                MR.strings.irc_notice_no_vips.desc()
            }

            "not_hosting" -> {
                MR.strings.irc_notice_not_hosting.desc()
            }

            "no_permission" -> {
                MR.strings.irc_notice_no_permission.desc()
            }

            "r9k_off" -> {
                MR.strings.irc_notice_r9k_off.desc()
            }

            "r9k_on" -> {
                MR.strings.irc_notice_r9k_on.desc()
            }

            "raid_error_already_raiding" -> {
                MR.strings.irc_notice_raid_error_already_raiding.desc()
            }

            "raid_error_forbidden" -> {
                MR.strings.irc_notice_raid_error_forbidden.desc()
            }

            "raid_error_self" -> {
                MR.strings.irc_notice_raid_error_self.desc()
            }

            "raid_error_too_many_viewers" -> {
                MR.strings.irc_notice_raid_error_too_many_viewers.desc()
            }

            "raid_error_unexpected" -> {
                MR.strings.irc_notice_raid_error_unexpected.format(
                    message?.substringAfter("a problem raiding ", "")
                        ?.substringBefore(". Please try", "") ?: "",
                )
            }

            "raid_notice_mature" -> {
                MR.strings.irc_notice_raid_notice_mature.desc()
            }

            "raid_notice_restricted_chat" -> {
                MR.strings.irc_notice_raid_notice_restricted_chat.desc()
            }

            "room_mods" -> {
                MR.strings.irc_notice_room_mods.format(
                    message?.substringAfter("this channel are: ", "") ?: "",
                )
            }

            "slow_off" -> {
                MR.strings.irc_notice_slow_off.desc()
            }

            "slow_on" -> {
                MR.strings.irc_notice_slow_on.format(
                    message?.substringAfter("send messages every ", "")
                        ?.substringBefore(" seconds.", "") ?: "",
                )
            }

            "subs_off" -> {
                MR.strings.irc_notice_subs_off.desc()
            }

            "subs_on" -> {
                MR.strings.irc_notice_subs_on.desc()
            }

            "timeout_no_timeout" -> {
                MR.strings.irc_notice_timeout_no_timeout.format(
                    message?.substringBefore(" is not timed", "") ?: "",
                )
            }

            "timeout_success" -> {
                MR.strings.irc_notice_timeout_success.format(
                    message?.substringBefore(" has been", "") ?: "",
                    message?.substringAfter("timed out for ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "tos_ban" -> {
                MR.strings.irc_notice_tos_ban.format(
                    message?.substringAfter("has closed channel ", "")
                        ?.substringBefore(" due to Terms", "") ?: "",
                )
            }

            "turbo_only_color" -> {
                MR.strings.irc_notice_turbo_only_color.format(
                    message?.substringAfter("following instead: ", "") ?: "",
                )
            }

            "unavailable_command" -> {
                MR.strings.irc_notice_unavailable_command.format(
                    message?.substringAfter("Sorry, “", "")
                        ?.substringBefore("” is not available", "")
                        ?: "",
                )
            }

            "unban_success" -> {
                MR.strings.irc_notice_unban_success.format(
                    message?.substringBefore(" is no longer", "") ?: "",
                )
            }

            "unmod_success" -> {
                MR.strings.irc_notice_unmod_success.format(
                    message?.substringAfter("You have removed ", "")
                        ?.substringBefore(" as a moderator", "") ?: "",
                )
            }

            "unraid_error_no_active_raid" -> {
                MR.strings.irc_notice_unraid_error_no_active_raid.desc()
            }

            "unraid_error_unexpected" -> {
                MR.strings.irc_notice_unraid_error_unexpected.desc()
            }

            "unraid_success" -> {
                MR.strings.irc_notice_unraid_success.desc()
            }

            "unrecognized_cmd" -> {
                MR.strings.irc_notice_unrecognized_cmd.format(
                    message?.substringAfter("Unrecognized command: ", "") ?: "",
                )
            }

            "untimeout_banned" -> {
                MR.strings.irc_notice_untimeout_banned.format(
                    message?.substringBefore(" is permanently banned", "") ?: "",
                )
            }

            "untimeout_success" -> {
                MR.strings.irc_notice_untimeout_success.format(
                    message?.substringBefore(" is no longer", "") ?: "",
                )
            }

            "unvip_success" -> {
                MR.strings.irc_notice_unvip_success.format(
                    message?.substringAfter("You have removed ", "")
                        ?.substringBefore(" as a VIP", "")
                        ?: "",
                )
            }

            "usage_ban" -> {
                MR.strings.irc_notice_usage_ban.desc()
            }

            "usage_clear" -> {
                MR.strings.irc_notice_usage_clear.desc()
            }

            "usage_color" -> {
                MR.strings.irc_notice_usage_color.format(
                    message?.substringAfter("following: ", "")?.substringBeforeLast(".", "") ?: "",
                )
            }

            "usage_commercial" -> {
                MR.strings.irc_notice_usage_commercial.desc()
            }

            "usage_disconnect" -> {
                MR.strings.irc_notice_usage_disconnect.desc()
            }

            "usage_delete" -> {
                MR.strings.irc_notice_usage_delete.desc()
            }

            "usage_emote_only_off" -> {
                MR.strings.irc_notice_usage_emote_only_off.desc()
            }

            "usage_emote_only_on" -> {
                MR.strings.irc_notice_usage_emote_only_on.desc()
            }

            "usage_followers_off" -> {
                MR.strings.irc_notice_usage_followers_off.desc()
            }

            "usage_followers_on" -> {
                MR.strings.irc_notice_usage_followers_on.desc()
            }

            "usage_help" -> {
                MR.strings.irc_notice_usage_help.desc()
            }

            "usage_host" -> {
                MR.strings.irc_notice_usage_host.desc()
            }

            "usage_marker" -> {
                MR.strings.irc_notice_usage_marker.desc()
            }

            "usage_me" -> {
                MR.strings.irc_notice_usage_me.desc()
            }

            "usage_mod" -> {
                MR.strings.irc_notice_usage_mod.desc()
            }

            "usage_mods" -> {
                MR.strings.irc_notice_usage_mods.desc()
            }

            "usage_r9k_off" -> {
                MR.strings.irc_notice_usage_r9k_off.desc()
            }

            "usage_r9k_on" -> {
                MR.strings.irc_notice_usage_r9k_on.desc()
            }

            "usage_raid" -> {
                MR.strings.irc_notice_usage_raid.desc()
            }

            "usage_slow_off" -> {
                MR.strings.irc_notice_usage_slow_off.desc()
            }

            "usage_slow_on" -> {
                MR.strings.irc_notice_usage_slow_on.format(
                    message?.substringAfter("default=", "")?.substringBefore(")", "") ?: "",
                )
            }

            "usage_subs_off" -> {
                MR.strings.irc_notice_usage_subs_off.desc()
            }

            "usage_subs_on" -> {
                MR.strings.irc_notice_usage_subs_on.desc()
            }

            "usage_timeout" -> {
                MR.strings.irc_notice_usage_timeout.desc()
            }

            "usage_unban" -> {
                MR.strings.irc_notice_usage_unban.desc()
            }

            "usage_unhost" -> {
                MR.strings.irc_notice_usage_unhost.desc()
            }

            "usage_unmod" -> {
                MR.strings.irc_notice_usage_unmod.desc()
            }

            "usage_unraid" -> {
                MR.strings.irc_notice_usage_unraid.desc()
            }

            "usage_untimeout" -> {
                MR.strings.irc_notice_usage_untimeout.desc()
            }

            "usage_unvip" -> {
                MR.strings.irc_notice_usage_unvip.desc()
            }

            "usage_user" -> {
                MR.strings.irc_notice_usage_user.desc()
            }

            "usage_vip" -> {
                MR.strings.irc_notice_usage_vip.desc()
            }

            "usage_vips" -> {
                MR.strings.irc_notice_usage_vips.desc()
            }

            "usage_whisper" -> {
                MR.strings.irc_notice_usage_whisper.desc()
            }

            "vip_success" -> {
                MR.strings.irc_notice_vip_success.format(
                    message?.substringAfter("You have added ", "")
                        ?.substringBeforeLast(" as a vip", "")
                        ?: "",
                )
            }

            "vips_success" -> {
                MR.strings.irc_notice_vips_success.format(
                    message?.substringAfter("channel are: ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "whisper_banned" -> {
                MR.strings.irc_notice_whisper_banned.desc()
            }

            "whisper_banned_recipient" -> {
                MR.strings.irc_notice_whisper_banned_recipient.desc()
            }

            "whisper_invalid_login" -> {
                MR.strings.irc_notice_whisper_invalid_login.desc()
            }

            "whisper_invalid_self" -> {
                MR.strings.irc_notice_whisper_invalid_self.desc()
            }

            "whisper_limit_per_min" -> {
                MR.strings.irc_notice_whisper_limit_per_min.desc()
            }

            "whisper_limit_per_sec" -> {
                MR.strings.irc_notice_whisper_limit_per_sec.desc()
            }

            "whisper_restricted" -> {
                MR.strings.irc_notice_whisper_restricted.desc()
            }

            "whisper_restricted_recipient" -> {
                MR.strings.irc_notice_whisper_restricted_recipient.desc()
            }

            else -> {
                null
            }
        }
    }

    private companion object {
        const val SUB_T1 = "1000"
        const val SUB_T2 = "2000"
        const val SUB_T3 = "3000"
        const val SUB_PRIME = "Prime"
    }
}

private fun String.getMentionsPrefix(): List<String> {
    return split(" ")
        .takeWhile { word ->
            word.startsWith(ChatPrefixConstants.ChatterPrefix)
        }
}
