package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Icon
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.*
import fr.outadoc.justchatting.utils.presentation.formatNumber
import fr.outadoc.justchatting.utils.resources.StringDesc2
import fr.outadoc.justchatting.utils.resources.desc2
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
                                    title = command.targetUserLogin.desc2(),
                                    titleIcon = Icon.Gavel,
                                    subtitle = Res.string.chat_ban.desc2(),
                                ),
                                body = null,
                            )
                        } else {
                            ChatListItem.Message.Highlighted(
                                timestamp = command.timestamp,
                                metadata = ChatListItem.Message.Highlighted.Metadata(
                                    title = command.targetUserLogin.desc2(),
                                    titleIcon = Icon.Gavel,
                                    subtitle = Res.string.chat_timeout.desc2(command.duration),
                                ),
                                body = null,
                            )
                        }
                    } else {
                        ChatListItem.Message.Notice(
                            timestamp = command.timestamp,
                            text = Res.string.chat_clear.desc2(),
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
                        ?: message.desc2(),
                )
            }

            is ChatEvent.Message.IncomingRaid -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc2(),
                        titleIcon = Icon.CallReceived,
                        subtitle = Res.string.chat_raid_header
                            .desc2(
                                Res.plurals.viewers.desc2(
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
                        title = userDisplayName.desc2(),
                        titleIcon = Icon.Cancel,
                        subtitle = Res.string.chat_unraid_subtitle.desc2(),
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.HighlightedMessage -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = Res.string.irc_msgid_highlighted_message.desc2(),
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
                        title = Res.string.irc_msgid_announcement.desc2(),
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
                        title = userDisplayName.desc2(),
                        titleIcon = when (subscriptionPlan) {
                            SUB_PRIME -> Icon.Star
                            else -> Icon.Star
                        },
                        subtitle = when (streakMonths) {
                            0 -> {
                                Res.string.chat_sub_header_withDuration
                                    .desc2(
                                        parseSubscriptionTier(subscriptionPlan),
                                        Res.plurals.months.desc2(
                                            number = cumulativeMonths,
                                            cumulativeMonths.formatNumber(),
                                        ),
                                    )
                            }

                            else -> {
                                Res.string.chat_sub_header_withDurationAndStreak.desc2(
                                    parseSubscriptionTier(subscriptionPlan),
                                    Res.plurals.months.desc2(
                                        number = cumulativeMonths,
                                        cumulativeMonths.formatNumber(),
                                    ),
                                    Res.plurals.months.desc2(
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
                        title = userDisplayName.desc2(),
                        titleIcon = Icon.Star,
                        subtitle = Res.string.chat_subConversion_header
                            .desc2(parseSubscriptionTierWithArticle(subscriptionPlan)),
                    ),
                    body = userMessage?.map(),
                )
            }

            is ChatEvent.Message.MassSubscriptionGift -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc2(),
                        titleIcon = Icon.VolunteerActivism,
                        subtitle = Res.string.chat_massSubGift_header
                            .desc2(
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
                        title = userDisplayName.desc2(),
                        titleIcon = Icon.Redeem,
                        subtitle = Res.string.chat_subGift_header
                            .desc2(
                                parseSubscriptionTier(subscriptionPlan),
                                recipientDisplayName,
                                Res.plurals.months.desc2(
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
                        title = userDisplayName.desc2(),
                        titleIcon = Icon.FastForward,
                        subtitle = when (priorGifterDisplayName) {
                            null -> {
                                Res.string.chat_subGift_payForwardAnonymous.desc2()
                            }

                            else -> {
                                Res.string.chat_subGift_payForward.desc2(priorGifterDisplayName)
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
                        title = systemMsg.desc2(),
                        titleIcon = null,
                        subtitle = null,
                    ),
                )
            }

            is ChatEvent.Message.Join -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = Res.string.chat_join.desc2(channelLogin),
                        subtitle = null,
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.SendError -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = Res.string.chat_send_msg_error.desc2(),
                        subtitle = null,
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.ChatMessage -> {
                val metadata = when {
                    isFirstMessageByUser -> {
                        ChatListItem.Message.Highlighted.Metadata(
                            title = Res.string.chat_first.desc2(),
                            titleIcon = Icon.WavingHand,
                            subtitle = null,
                        )
                    }

                    rewardId != null -> {
                        ChatListItem.Message.Highlighted.Metadata(
                            title = Res.string.chat_reward.desc2(),
                            titleIcon = Icon.Toll,
                            subtitle = null,
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
                        title = Res.plurals.user_redeemed
                            .desc2(
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

    private fun parseSubscriptionTier(planId: String): StringDesc2 {
        return when (planId) {
            SUB_T1 -> Res.string.chat_sub_tier1.desc2()
            SUB_T2 -> Res.string.chat_sub_tier2.desc2()
            SUB_T3 -> Res.string.chat_sub_tier3.desc2()
            SUB_PRIME -> Res.string.chat_sub_prime.desc2()
            else -> planId.desc2()
        }
    }

    private fun parseSubscriptionTierWithArticle(planId: String): StringDesc2 {
        return when (planId) {
            SUB_T1 -> Res.string.chat_subGift_tier1.desc2()
            SUB_T2 -> Res.string.chat_subGift_tier2.desc2()
            SUB_T3 -> Res.string.chat_subGift_tier3.desc2()
            else -> planId.desc2()
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
                    mentions = mentions
                        .map { mention ->
                            mention.drop(1)
                        }
                        .toImmutableList(),
                )
            } else {
                null
            },
        )
    }

    private fun getLabelForNotice(messageId: String?, message: String?): StringDesc2? {
        return when (messageId) {
            "already_banned" -> {
                Res.string.irc_notice_already_banned
                    .desc2(
                        message?.substringBefore(" is already banned", "") ?: "",
                    )
            }

            "already_emote_only_off" -> {
                Res.string.irc_notice_already_emote_only_off.desc2()
            }

            "already_emote_only_on" -> {
                Res.string.irc_notice_already_emote_only_on.desc2()
            }

            "already_followers_off" -> {
                Res.string.irc_notice_already_followers_off.desc2()
            }

            "already_followers_on" -> {
                Res.string.irc_notice_already_followers_on.desc2(
                    message?.substringAfter("is already in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                )
            }

            "already_r9k_off" -> {
                Res.string.irc_notice_already_r9k_off.desc2()
            }

            "already_r9k_on" -> {
                Res.string.irc_notice_already_r9k_on.desc2()
            }

            "already_slow_off" -> {
                Res.string.irc_notice_already_slow_off.desc2()
            }

            "already_slow_on" -> {
                Res.string.irc_notice_already_slow_on.desc2(
                    message?.substringAfter("is already in ", "")
                        ?.substringBefore("-second slow", "")
                        ?: "",
                )
            }

            "already_subs_off" -> {
                Res.string.irc_notice_already_subs_off.desc2()
            }

            "already_subs_on" -> {
                Res.string.irc_notice_already_subs_on.desc2()
            }

            "autohost_receive" -> {
                Res.string.irc_notice_autohost_receive.desc2(
                    message?.substringBefore(" is now auto hosting", "") ?: "",
                    message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "")
                        ?: "",
                )
            }

            "bad_ban_admin" -> {
                Res.string.irc_notice_bad_ban_admin.desc2(
                    message?.substringAfter("cannot ban admin", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_ban_anon" -> {
                Res.string.irc_notice_bad_ban_anon.desc2()
            }

            "bad_ban_broadcaster" -> {
                Res.string.irc_notice_bad_ban_broadcaster.desc2()
            }

            "bad_ban_mod" -> {
                Res.string.irc_notice_bad_ban_mod.desc2(
                    message?.substringAfter("cannot ban moderator", "")
                        ?.substringBefore(" unless you are", "") ?: "",
                )
            }

            "bad_ban_self" -> {
                Res.string.irc_notice_bad_ban_self.desc2()
            }

            "bad_ban_staff" -> {
                Res.string.irc_notice_bad_ban_staff.desc2(
                    message?.substringAfter("cannot ban staff", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_commercial_error" -> {
                Res.string.irc_notice_bad_commercial_error.desc2()
            }

            "bad_delete_message_broadcaster" -> {
                Res.string.irc_notice_bad_delete_message_broadcaster.desc2()
            }

            "bad_delete_message_mod" -> {
                Res.string.irc_notice_bad_delete_message_mod.desc2(
                    message?.substringAfter("from another moderator ", "")
                        ?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "bad_host_error" -> {
                Res.string.irc_notice_bad_host_error.desc2(
                    message?.substringAfter("a problem hosting ", "")
                        ?.substringBefore(". Please try", "") ?: "",
                )
            }

            "bad_host_hosting" -> {
                Res.string.irc_notice_bad_host_hosting.desc2(
                    message?.substringAfter("is already hosting ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "bad_host_rate_exceeded" -> {
                Res.string.irc_notice_bad_host_rate_exceeded.desc2(
                    message?.substringAfter("changed more than ", "")
                        ?.substringBefore(" times every half", "") ?: "",
                )
            }

            "bad_host_rejected" -> {
                Res.string.irc_notice_bad_host_rejected.desc2()
            }

            "bad_host_self" -> {
                Res.string.irc_notice_bad_host_self.desc2()
            }

            "bad_mod_banned" -> {
                Res.string.irc_notice_bad_mod_banned.desc2(
                    message?.substringBefore(" is banned", "") ?: "",
                )
            }

            "bad_mod_mod" -> {
                Res.string.irc_notice_bad_mod_mod.desc2(
                    message?.substringBefore(" is already", "") ?: "",
                )
            }

            "bad_slow_duration" -> {
                Res.string.irc_notice_bad_slow_duration.desc2(
                    message?.substringAfter("to more than ", "")?.substringBefore(" seconds.", "")
                        ?: "",
                )
            }

            "bad_timeout_admin" -> {
                Res.string.irc_notice_bad_timeout_admin.desc2(
                    message?.substringAfter("cannot timeout admin ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_timeout_anon" -> {
                Res.string.irc_notice_bad_timeout_anon.desc2()
            }

            "bad_timeout_broadcaster" -> {
                Res.string.irc_notice_bad_timeout_broadcaster.desc2()
            }

            "bad_timeout_duration" -> {
                Res.string.irc_notice_bad_timeout_duration.desc2(
                    message?.substringAfter("for more than ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "bad_timeout_mod" -> {
                Res.string.irc_notice_bad_timeout_mod.desc2(
                    message?.substringAfter("cannot timeout moderator ", "")
                        ?.substringBefore(" unless you are", "") ?: "",
                )
            }

            "bad_timeout_self" -> {
                Res.string.irc_notice_bad_timeout_self.desc2()
            }

            "bad_timeout_staff" -> {
                Res.string.irc_notice_bad_timeout_staff.desc2(
                    message?.substringAfter("cannot timeout staff ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_unban_no_ban" -> {
                Res.string.irc_notice_bad_unban_no_ban.desc2(
                    message?.substringBefore(" is not banned", "") ?: "",
                )
            }

            "bad_unhost_error" -> {
                Res.string.irc_notice_bad_unhost_error.desc2()
            }

            "bad_unmod_mod" -> {
                Res.string.irc_notice_bad_unmod_mod.desc2(
                    message?.substringBefore(" is not a", "") ?: "",
                )
            }

            "bad_vip_grantee_banned" -> {
                Res.string.irc_notice_bad_vip_grantee_banned.desc2(
                    message?.substringBefore(" is banned in", "") ?: "",
                )
            }

            "bad_vip_grantee_already_vip" -> {
                Res.string.irc_notice_bad_vip_grantee_already_vip.desc2(
                    message?.substringBefore(" is already a", "") ?: "",
                )
            }

            "bad_vip_max_vips_reached" -> {
                Res.string.irc_notice_bad_vip_max_vips_reached.desc2()
            }

            "bad_vip_achievement_incomplete" -> {
                Res.string.irc_notice_bad_vip_achievement_incomplete.desc2()
            }

            "bad_unvip_grantee_not_vip" -> {
                Res.string.irc_notice_bad_unvip_grantee_not_vip.desc2(
                    message?.substringBefore(" is not a", "") ?: "",
                )
            }

            "ban_success" -> {
                Res.string.irc_notice_ban_success.desc2(
                    message?.substringBefore(" is now banned", "") ?: "",
                )
            }

            "cmds_available" -> {
                Res.string.irc_notice_cmds_available.desc2(
                    message?.substringAfter("details): ", "")?.substringBefore(" More help:", "")
                        ?: "",
                )
            }

            "color_changed" -> {
                Res.string.irc_notice_color_changed.desc2()
            }

            "commercial_success" -> {
                Res.string.irc_notice_commercial_success.desc2(
                    message?.substringAfter("Initiating ", "")
                        ?.substringBefore(" second commercial break.", "") ?: "",
                )
            }

            "delete_message_success" -> {
                Res.string.irc_notice_delete_message_success.desc2(
                    message?.substringAfter("The message from ", "")
                        ?.substringBefore(" is now deleted.", "") ?: "",
                )
            }

            "delete_staff_message_success" -> {
                Res.string.irc_notice_delete_staff_message_success.desc2(
                    message?.substringAfter("message from staff ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "emote_only_off" -> {
                Res.string.irc_notice_emote_only_off.desc2()
            }

            "emote_only_on" -> {
                Res.string.irc_notice_emote_only_on.desc2()
            }

            "followers_off" -> {
                Res.string.irc_notice_followers_off.desc2()
            }

            "followers_on" -> {
                Res.string.irc_notice_followers_on.desc2(
                    message?.substringAfter("is now in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                )
            }

            "followers_on_zero" -> {
                Res.string.irc_notice_followers_on_zero.desc2()
            }

            "host_off" -> {
                Res.string.irc_notice_host_off.desc2()
            }

            "host_on" -> {
                Res.string.irc_notice_host_on.desc2(
                    message?.substringAfter("Now hosting ", "")?.substringBeforeLast(".", "") ?: "",
                )
            }

            "host_receive" -> {
                Res.string.irc_notice_host_receive.desc2(
                    message?.substringBefore(" is now hosting", "") ?: "",
                    message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "")
                        ?: "",
                )
            }

            "host_receive_no_count" -> {
                Res.string.irc_notice_host_receive_no_count.desc2(
                    message?.substringBefore(" is now hosting", "") ?: "",
                )
            }

            "host_target_went_offline" -> {
                Res.string.irc_notice_host_target_went_offline.desc2(
                    message?.substringBefore(" has gone offline", "") ?: "",
                )
            }

            "hosts_remaining" -> {
                Res.string.irc_notice_hosts_remaining.desc2(
                    message?.substringBefore(" host commands", "") ?: "",
                )
            }

            "invalid_user" -> {
                Res.string.irc_notice_invalid_user.desc2(
                    message?.substringAfter("Invalid username: ", "") ?: "",
                )
            }

            "mod_success" -> {
                Res.string.irc_notice_mod_success.desc2(
                    message?.substringAfter("You have added ", "")
                        ?.substringBefore(" as a moderator", "") ?: "",
                )
            }

            "msg_banned" -> {
                Res.string.irc_notice_msg_banned.desc2(
                    message?.substringAfter("from talking in ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "msg_bad_characters" -> {
                Res.string.irc_notice_msg_bad_characters.desc2()
            }

            "msg_channel_blocked" -> {
                Res.string.irc_notice_msg_channel_blocked.desc2()
            }

            "msg_channel_suspended" -> {
                Res.string.irc_notice_msg_channel_suspended.desc2()
            }

            "msg_duplicate" -> {
                Res.string.irc_notice_msg_duplicate.desc2()
            }

            "msg_emoteonly" -> {
                Res.string.irc_notice_msg_emoteonly.desc2()
            }

            "msg_followersonly" -> {
                Res.string.irc_notice_msg_followersonly.desc2(
                    message?.substringAfter("This room is in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                    message?.substringAfter("Follow ", "")?.substringBefore(" to join", "") ?: "",
                )
            }

            "msg_followersonly_followed" -> {
                Res.string.irc_notice_msg_followersonly_followed.desc2(
                    message?.substringAfter("This room is in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                    message?.substringAfter("following for ", "")?.substringBefore(". Continue", "")
                        ?: "",
                )
            }

            "msg_followersonly_zero" -> {
                Res.string.irc_notice_msg_followersonly_zero.desc2(
                    message?.substringAfter(". Follow ", "")?.substringBefore(" to join the", "")
                        ?: "",
                )
            }

            "msg_r9k" -> {
                Res.string.irc_notice_msg_r9k.desc2()
            }

            "msg_ratelimit" -> {
                Res.string.irc_notice_msg_ratelimit.desc2()
            }

            "msg_rejected" -> {
                Res.string.irc_notice_msg_rejected.desc2()
            }

            "msg_rejected_mandatory" -> {
                Res.string.irc_notice_msg_rejected_mandatory.desc2()
            }

            "msg_slowmode" -> {
                Res.string.irc_notice_msg_slowmode.desc2(
                    message?.substringAfter("talk again in ", "")?.substringBefore(" seconds.", "")
                        ?: "",
                )
            }

            "msg_subsonly" -> {
                Res.string.irc_notice_msg_subsonly.desc2(
                    message?.substringAfter("/products/", "")?.substringBefore("/ticket?ref", "")
                        ?: "",
                )
            }

            "msg_suspended" -> {
                Res.string.irc_notice_msg_suspended.desc2()
            }

            "msg_timedout" -> {
                Res.string.irc_notice_msg_timedout.desc2(
                    message?.substringAfter("timed out for ", "")
                        ?.substringBefore(" more seconds.", "")
                        ?: "",
                )
            }

            "msg_verified_email" -> {
                Res.string.irc_notice_msg_verified_email.desc2()
            }

            "no_help" -> {
                Res.string.irc_notice_no_help.desc2()
            }

            "no_mods" -> {
                Res.string.irc_notice_no_mods.desc2()
            }

            "no_vips" -> {
                Res.string.irc_notice_no_vips.desc2()
            }

            "not_hosting" -> {
                Res.string.irc_notice_not_hosting.desc2()
            }

            "no_permission" -> {
                Res.string.irc_notice_no_permission.desc2()
            }

            "r9k_off" -> {
                Res.string.irc_notice_r9k_off.desc2()
            }

            "r9k_on" -> {
                Res.string.irc_notice_r9k_on.desc2()
            }

            "raid_error_already_raiding" -> {
                Res.string.irc_notice_raid_error_already_raiding.desc2()
            }

            "raid_error_forbidden" -> {
                Res.string.irc_notice_raid_error_forbidden.desc2()
            }

            "raid_error_self" -> {
                Res.string.irc_notice_raid_error_self.desc2()
            }

            "raid_error_too_many_viewers" -> {
                Res.string.irc_notice_raid_error_too_many_viewers.desc2()
            }

            "raid_error_unexpected" -> {
                Res.string.irc_notice_raid_error_unexpected.desc2(
                    message?.substringAfter("a problem raiding ", "")
                        ?.substringBefore(". Please try", "") ?: "",
                )
            }

            "raid_notice_mature" -> {
                Res.string.irc_notice_raid_notice_mature.desc2()
            }

            "raid_notice_restricted_chat" -> {
                Res.string.irc_notice_raid_notice_restricted_chat.desc2()
            }

            "room_mods" -> {
                Res.string.irc_notice_room_mods.desc2(
                    message?.substringAfter("this channel are: ", "") ?: "",
                )
            }

            "slow_off" -> {
                Res.string.irc_notice_slow_off.desc2()
            }

            "slow_on" -> {
                Res.string.irc_notice_slow_on.desc2(
                    message?.substringAfter("send messages every ", "")
                        ?.substringBefore(" seconds.", "") ?: "",
                )
            }

            "subs_off" -> {
                Res.string.irc_notice_subs_off.desc2()
            }

            "subs_on" -> {
                Res.string.irc_notice_subs_on.desc2()
            }

            "timeout_no_timeout" -> {
                Res.string.irc_notice_timeout_no_timeout.desc2(
                    message?.substringBefore(" is not timed", "") ?: "",
                )
            }

            "timeout_success" -> {
                Res.string.irc_notice_timeout_success.desc2(
                    message?.substringBefore(" has been", "") ?: "",
                    message?.substringAfter("timed out for ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "tos_ban" -> {
                Res.string.irc_notice_tos_ban.desc2(
                    message?.substringAfter("has closed channel ", "")
                        ?.substringBefore(" due to Terms", "") ?: "",
                )
            }

            "turbo_only_color" -> {
                Res.string.irc_notice_turbo_only_color.desc2(
                    message?.substringAfter("following instead: ", "") ?: "",
                )
            }

            "unavailable_command" -> {
                Res.string.irc_notice_unavailable_command.desc2(
                    message?.substringAfter("Sorry, “", "")
                        ?.substringBefore("” is not available", "")
                        ?: "",
                )
            }

            "unban_success" -> {
                Res.string.irc_notice_unban_success.desc2(
                    message?.substringBefore(" is no longer", "") ?: "",
                )
            }

            "unmod_success" -> {
                Res.string.irc_notice_unmod_success.desc2(
                    message?.substringAfter("You have removed ", "")
                        ?.substringBefore(" as a moderator", "") ?: "",
                )
            }

            "unraid_error_no_active_raid" -> {
                Res.string.irc_notice_unraid_error_no_active_raid.desc2()
            }

            "unraid_error_unexpected" -> {
                Res.string.irc_notice_unraid_error_unexpected.desc2()
            }

            "unraid_success" -> {
                Res.string.irc_notice_unraid_success.desc2()
            }

            "unrecognized_cmd" -> {
                Res.string.irc_notice_unrecognized_cmd.desc2(
                    message?.substringAfter("Unrecognized command: ", "") ?: "",
                )
            }

            "untimeout_banned" -> {
                Res.string.irc_notice_untimeout_banned.desc2(
                    message?.substringBefore(" is permanently banned", "") ?: "",
                )
            }

            "untimeout_success" -> {
                Res.string.irc_notice_untimeout_success.desc2(
                    message?.substringBefore(" is no longer", "") ?: "",
                )
            }

            "unvip_success" -> {
                Res.string.irc_notice_unvip_success.desc2(
                    message?.substringAfter("You have removed ", "")
                        ?.substringBefore(" as a VIP", "")
                        ?: "",
                )
            }

            "usage_ban" -> {
                Res.string.irc_notice_usage_ban.desc2()
            }

            "usage_clear" -> {
                Res.string.irc_notice_usage_clear.desc2()
            }

            "usage_color" -> {
                Res.string.irc_notice_usage_color.desc2(
                    message?.substringAfter("following: ", "")?.substringBeforeLast(".", "") ?: "",
                )
            }

            "usage_commercial" -> {
                Res.string.irc_notice_usage_commercial.desc2()
            }

            "usage_disconnect" -> {
                Res.string.irc_notice_usage_disconnect.desc2()
            }

            "usage_delete" -> {
                Res.string.irc_notice_usage_delete.desc2()
            }

            "usage_emote_only_off" -> {
                Res.string.irc_notice_usage_emote_only_off.desc2()
            }

            "usage_emote_only_on" -> {
                Res.string.irc_notice_usage_emote_only_on.desc2()
            }

            "usage_followers_off" -> {
                Res.string.irc_notice_usage_followers_off.desc2()
            }

            "usage_followers_on" -> {
                Res.string.irc_notice_usage_followers_on.desc2()
            }

            "usage_help" -> {
                Res.string.irc_notice_usage_help.desc2()
            }

            "usage_host" -> {
                Res.string.irc_notice_usage_host.desc2()
            }

            "usage_marker" -> {
                Res.string.irc_notice_usage_marker.desc2()
            }

            "usage_me" -> {
                Res.string.irc_notice_usage_me.desc2()
            }

            "usage_mod" -> {
                Res.string.irc_notice_usage_mod.desc2()
            }

            "usage_mods" -> {
                Res.string.irc_notice_usage_mods.desc2()
            }

            "usage_r9k_off" -> {
                Res.string.irc_notice_usage_r9k_off.desc2()
            }

            "usage_r9k_on" -> {
                Res.string.irc_notice_usage_r9k_on.desc2()
            }

            "usage_raid" -> {
                Res.string.irc_notice_usage_raid.desc2()
            }

            "usage_slow_off" -> {
                Res.string.irc_notice_usage_slow_off.desc2()
            }

            "usage_slow_on" -> {
                Res.string.irc_notice_usage_slow_on.desc2(
                    message?.substringAfter("default=", "")?.substringBefore(")", "") ?: "",
                )
            }

            "usage_subs_off" -> {
                Res.string.irc_notice_usage_subs_off.desc2()
            }

            "usage_subs_on" -> {
                Res.string.irc_notice_usage_subs_on.desc2()
            }

            "usage_timeout" -> {
                Res.string.irc_notice_usage_timeout.desc2()
            }

            "usage_unban" -> {
                Res.string.irc_notice_usage_unban.desc2()
            }

            "usage_unhost" -> {
                Res.string.irc_notice_usage_unhost.desc2()
            }

            "usage_unmod" -> {
                Res.string.irc_notice_usage_unmod.desc2()
            }

            "usage_unraid" -> {
                Res.string.irc_notice_usage_unraid.desc2()
            }

            "usage_untimeout" -> {
                Res.string.irc_notice_usage_untimeout.desc2()
            }

            "usage_unvip" -> {
                Res.string.irc_notice_usage_unvip.desc2()
            }

            "usage_user" -> {
                Res.string.irc_notice_usage_user.desc2()
            }

            "usage_vip" -> {
                Res.string.irc_notice_usage_vip.desc2()
            }

            "usage_vips" -> {
                Res.string.irc_notice_usage_vips.desc2()
            }

            "usage_whisper" -> {
                Res.string.irc_notice_usage_whisper.desc2()
            }

            "vip_success" -> {
                Res.string.irc_notice_vip_success.desc2(
                    message?.substringAfter("You have added ", "")
                        ?.substringBeforeLast(" as a vip", "")
                        ?: "",
                )
            }

            "vips_success" -> {
                Res.string.irc_notice_vips_success.desc2(
                    message?.substringAfter("channel are: ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "whisper_banned" -> {
                Res.string.irc_notice_whisper_banned.desc2()
            }

            "whisper_banned_recipient" -> {
                Res.string.irc_notice_whisper_banned_recipient.desc2()
            }

            "whisper_invalid_login" -> {
                Res.string.irc_notice_whisper_invalid_login.desc2()
            }

            "whisper_invalid_self" -> {
                Res.string.irc_notice_whisper_invalid_self.desc2()
            }

            "whisper_limit_per_min" -> {
                Res.string.irc_notice_whisper_limit_per_min.desc2()
            }

            "whisper_limit_per_sec" -> {
                Res.string.irc_notice_whisper_limit_per_sec.desc2()
            }

            "whisper_restricted" -> {
                Res.string.irc_notice_whisper_restricted.desc2()
            }

            "whisper_restricted_recipient" -> {
                Res.string.irc_notice_whisper_restricted_recipient.desc2()
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
