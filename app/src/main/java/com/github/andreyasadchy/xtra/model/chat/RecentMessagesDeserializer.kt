package com.github.andreyasadchy.xtra.model.chat

import android.content.Context
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.XtraApp
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.chat.MessageListenerImpl
import com.github.andreyasadchy.xtra.util.prefs
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.util.*

class RecentMessagesDeserializer : JsonDeserializer<RecentMessagesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RecentMessagesResponse {
        val messages = mutableListOf<LiveChatMessage>()
        for (i in json.asJsonObject.getAsJsonArray("messages")) {
            val appContext = XtraApp.INSTANCE.applicationContext
            val message = i.asString
            val chatMsg = when {
                message.contains("PRIVMSG") -> onMessage(appContext, message, false)
                message.contains("USERNOTICE") -> onMessage(appContext, message, true)
                message.contains("CLEARMSG") -> onClearMessage(appContext, message)
                message.contains("CLEARCHAT") -> onClearChat(appContext, message)
                message.contains("NOTICE") -> onNotice(appContext, message)
                else -> null
            }
            if (chatMsg != null) {
                messages.add(chatMsg)
            }
        }
        return RecentMessagesResponse(messages)
    }

    private fun onMessage(context: Context, message: String, userNotice: Boolean): LiveChatMessage? {
        if (!userNotice || (userNotice && context.prefs().getBoolean(C.CHAT_SHOW_USERNOTICE, true))) {
            val parts = message.substring(1).split(" ".toRegex(), 2)
            val prefix = parts[0]
            val prefixes = splitAndMakeMap(prefix, ";", "=")
            val messageInfo = parts[1] //:<user>!<user>@<user>.tmi.twitch.tv PRIVMSG #<channelName> :<message>
            val userLogin = prefixes["login"] ?: try {
                messageInfo.substring(1, messageInfo.indexOf("!"))
            } catch (e: Exception) {
                null
            }
            val systemMsg = prefixes["system-msg"]?.replace("\\s", " ")
            val msgIndex = messageInfo.indexOf(" ", messageInfo.indexOf("#", messageInfo.indexOf(":") + 1) + 1)
            if (msgIndex == -1 && userNotice) { // no user message & is user notice
                return LiveChatMessage(
                    message = systemMsg ?: messageInfo,
                    color = "#999999",
                    isAction = true,
                    timestamp = prefixes["tmi-sent-ts"]?.toLong(),
                    fullMsg = message
                )
            } else {
                val userMessage: String
                val isAction: Boolean
                messageInfo.substring(if (messageInfo.substring(msgIndex + 1).startsWith(":")) msgIndex + 2 else msgIndex + 1).let { //from <message>
                    if (!it.startsWith(MessageListenerImpl.ACTION)) {
                        userMessage = it
                        isAction = false
                    } else {
                        userMessage = it.substring(8, it.lastIndex)
                        isAction = true
                    }
                }
                var emotesList: MutableList<TwitchEmote>? = null
                val emotes = prefixes["emotes"]
                if (emotes != null) {
                    val entries = splitAndMakeMap(emotes, "/", ":").entries
                    emotesList = ArrayList(entries.size)
                    entries.forEach { emote ->
                        emote.value?.split(",")?.forEach { indexes ->
                            val index = indexes.split("-")
                            emotesList.add(TwitchEmote(emote.key, index[0].toInt(), index[1].toInt()))
                        }
                    }
                }
                var badgesList: MutableList<Badge>? = null
                val badges = prefixes["badges"]
                if (badges != null) {
                    val entries = splitAndMakeMap(badges, ",", "/").entries
                    badgesList = ArrayList(entries.size)
                    entries.forEach {
                        it.value?.let { value ->
                            badgesList.add(Badge(it.key, value))
                        }
                    }
                }
                return LiveChatMessage(
                    id = prefixes["id"],
                    userId = prefixes["user-id"],
                    userLogin = userLogin,
                    userName = prefixes["display-name"]?.replace("\\s", " "),
                    message = userMessage,
                    color = prefixes["color"],
                    isAction = isAction,
                    rewardId = prefixes["custom-reward-id"],
                    isFirst = prefixes["first-msg"] == "1",
                    msgId = prefixes["msg-id"],
                    systemMsg = systemMsg,
                    emotes = emotesList,
                    badges = badgesList,
                    timestamp = prefixes["tmi-sent-ts"]?.toLong(),
                    fullMsg = message
                )
            }
        } else {
            return null
        }
    }

    private fun onClearMessage(context: Context, message: String): LiveChatMessage? {
        if (context.prefs().getBoolean(C.CHAT_SHOW_CLEARMSG, true)) {
            val parts = message.substring(1).split(" ".toRegex(), 2)
            val prefix = parts[0]
            val prefixes = splitAndMakeMap(prefix, ";", "=")
            val user = prefixes["login"]
            val messageInfo = parts[1]
            val msgIndex = messageInfo.indexOf(":", messageInfo.indexOf(":") + 1)
            val index2 = messageInfo.indexOf(" ", messageInfo.indexOf("#") + 1)
            val msg = messageInfo.substring(if (msgIndex != -1) msgIndex + 1 else index2 + 1)
            return LiveChatMessage(
                message = context.getString(R.string.chat_clearmsg, user, msg),
                color = "#999999",
                isAction = true,
                timestamp = prefixes["tmi-sent-ts"]?.toLong(),
                fullMsg = message
            )
        } else {
            return null
        }
    }

    private fun onClearChat(context: Context, message: String): LiveChatMessage? {
        if (context.prefs().getBoolean(C.CHAT_SHOW_CLEARCHAT, true)) {
            val parts = message.substring(1).split(" ".toRegex(), 2)
            val prefix = parts[0]
            val prefixes = splitAndMakeMap(prefix, ";", "=")
            val duration = prefixes["ban-duration"]
            val messageInfo = parts[1]
            val userIndex = messageInfo.indexOf(":", messageInfo.indexOf(":") + 1)
            val index2 = messageInfo.indexOf(" ", messageInfo.indexOf("#") + 1)
            val user = if (userIndex != -1) messageInfo.substring(userIndex + 1) else if (index2 != -1) messageInfo.substring(index2 + 1) else null
            val type = if (user == null) { "clearchat" } else { if (duration != null) { "timeout" } else { "ban" } }
            return LiveChatMessage(
                message = when (type) {
                    "clearchat" -> context.getString(R.string.chat_clear)
                    "timeout" -> context.getString(R.string.chat_timeout, user, TwitchApiHelper.getDurationFromSeconds(context, duration))
                    "ban" -> context.getString(R.string.chat_ban, user)
                    else -> return null
                },
                color = "#999999",
                isAction = true,
                timestamp = prefixes["tmi-sent-ts"]?.toLong(),
                fullMsg = message
            )
        } else {
            return null
        }
    }

    private fun onNotice(context: Context, message: String): LiveChatMessage {
        val parts = message.substring(1).split(" ".toRegex(), 2)
        val prefix = parts[0]
        val prefixes = splitAndMakeMap(prefix, ";", "=")
        val messageInfo = parts[1]
        val msgId = prefixes["msg-id"]
        val msgIndex = messageInfo.indexOf(":", messageInfo.indexOf(":") + 1)
        val index2 = messageInfo.indexOf(" ", messageInfo.indexOf("#") + 1)
        val msg = messageInfo.substring(if (msgIndex != -1) msgIndex + 1 else index2 + 1)
        val lang = Locale.getDefault().language
        return LiveChatMessage(
            message = if (lang == "ar" || lang == "es" || lang == "ja" || lang == "pt" || lang == "ru") {
                TwitchApiHelper.getNoticeString(context, msgId, msg) ?: msg
            } else {
                msg
            },
            color = "#999999",
            isAction = true,
            fullMsg = message
        )
    }

    private fun onUserNotice(context: Context, message: String): LiveChatMessage? {
        if (context.prefs().getBoolean(C.CHAT_SHOW_USERNOTICE, true)) {
            val parts = message.substring(1).split(" ".toRegex(), 2)
            val prefix = parts[0]
            val prefixes = splitAndMakeMap(prefix, ";", "=")
            val system = prefixes["system-msg"]?.replace("\\s", " ")
            val messageInfo = parts[1]
            val msgIndex = messageInfo.indexOf(":", messageInfo.indexOf(":") + 1)
            val index2 = messageInfo.indexOf(" ", messageInfo.indexOf("#") + 1)
            val msg = if (msgIndex != -1) messageInfo.substring(msgIndex + 1) else if (index2 != -1) messageInfo.substring(index2 + 1) else null
            var emotesList: MutableList<TwitchEmote>? = null
            val emotes = prefixes["emotes"]
            if (emotes != null && system != null && msg != null) {
                val entries = splitAndMakeMap(emotes, "/", ":").entries
                emotesList = ArrayList(entries.size)
                entries.forEach { emote ->
                    emote.value?.split(",")?.forEach { indexes ->
                        val index = indexes.split("-")
                        emotesList.add(TwitchEmote(emote.key, index[0].toInt() + system.length + 1, index[1].toInt() + system.length + 1))
                    }
                }
            }
            return LiveChatMessage(
                message = if (system != null) if (msg != null) "$system $msg" else system else msg,
                color = "#999999",
                isAction = true,
                emotes = emotesList,
                timestamp = prefixes["tmi-sent-ts"]?.toLong(),
                fullMsg = message
            )
        } else {
            return null
        }
    }

    private fun splitAndMakeMap(string: String, splitRegex: String, mapRegex: String): Map<String, String?> {
        val list = string.split(splitRegex.toRegex()).dropLastWhile { it.isEmpty() }
        val map = LinkedHashMap<String, String?>()
        for (pair in list) {
            val kv = pair.split(mapRegex.toRegex()).dropLastWhile { it.isEmpty() }
            map[kv[0]] = if (kv.size == 2) kv[1] else null
        }
        return map
    }
}
