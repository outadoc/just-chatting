package com.github.andreyasadchy.xtra.ui.common

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.getSpans
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.ChatMessage
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.Image
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.PubSubPointReward
import com.github.andreyasadchy.xtra.model.chat.TwitchBadge
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import java.util.Random
import kotlin.collections.set

class ChatAdapter(
    private val context: Context,
    private val emoteSize: Int,
    private val badgeSize: Int,
    private val randomColor: Boolean,
    private val enableTimestamps: Boolean,
    private val timestampFormat: String?,
    private val firstMsgVisibility: String?,
    private val firstChatMsg: String,
    private val rewardChatMsg: String,
    private val redeemedChatMsg: String,
    private val redeemedNoMsg: String,
    private val animateGifs: Boolean,
    private val emoteQuality: String
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    var messages: MutableList<ChatMessage>? = null
        set(value) {
            val oldSize = field?.size ?: 0
            if (oldSize > 0) {
                notifyItemRangeRemoved(0, oldSize)
            }
            field = value
        }

    private val twitchColors = intArrayOf(
        -65536,
        -16776961,
        -16744448,
        -5103070,
        -32944,
        -6632142,
        -47872,
        -13726889,
        -2448096,
        -2987746,
        -10510688,
        -14774017,
        -38476,
        -7722014,
        -16711809
    )

    private val noColor = -10066329

    private val random = Random()
    private val userColors = HashMap<String, Int>()
    private val savedColors = HashMap<String, Int>()
    private var globalBadges: List<TwitchBadge>? = null
    private var channelBadges: List<TwitchBadge>? = null
    private val emotes = HashMap<String, Emote>()
    private var cheerEmotes: List<CheerEmote>? = null
    private var loggedInUser: String? = null
    private val scaledEmoteSize = (emoteSize * 0.78f).toInt()

    fun interface OnMessageClickListener {
        fun onMessageClick(
            originalMessage: CharSequence,
            formattedMessage: CharSequence,
            userId: String?,
            fullMsg: String?
        )
    }

    private var messageClickListener: OnMessageClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.chat_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = messages?.get(position) ?: return
        val liveMessage = chatMessage as? LiveChatMessage
        val pointReward = chatMessage as? PubSubPointReward ?: liveMessage?.pointReward
        val builder = SpannableStringBuilder()
        val images = ArrayList<Image>()
        var imageIndex = 0
        var badgesCount = 0
        val systemMsg = liveMessage?.systemMsg

        if (systemMsg != null) {
            builder.append("$systemMsg\n")
            imageIndex += systemMsg.length + 1
        } else {
            val msgId = liveMessage?.msgId?.let {
                TwitchApiHelper.getMessageIdString(it) ?: liveMessage.msgId
            }
            if (msgId != null) {
                builder.append("$msgId\n")
                imageIndex += msgId.length + 1
            }
        }

        if (liveMessage?.isFirst == true && firstMsgVisibility == "0") {
            builder.append("$firstChatMsg\n")
            imageIndex += firstChatMsg.length + 1
        }

        if (liveMessage?.rewardId != null && pointReward == null && firstMsgVisibility == "0") {
            builder.append("$rewardChatMsg\n")
            imageIndex += rewardChatMsg.length + 1
        }

        if (!pointReward?.message.isNullOrBlank()) {
            val string = redeemedChatMsg.format(pointReward?.rewardTitle)
            builder.append("$string ")
            imageIndex += string.length + 1

            val url = when (emoteQuality) {
                "4" -> pointReward?.rewardImage?.url4
                "3" -> pointReward?.rewardImage?.url4
                "2" -> pointReward?.rewardImage?.url2
                else -> pointReward?.rewardImage?.url1
            }

            url?.let {
                builder.append("  ")
                images.add(Image(it, imageIndex++, imageIndex++, false))
                badgesCount++
            }

            builder.append("${pointReward?.rewardCost}\n")
            imageIndex += (pointReward?.rewardCost?.toString()?.length ?: 0) + 1
        }

        val timestamp =
            liveMessage?.timestamp?.let { TwitchApiHelper.getTimestamp(it, timestampFormat) }
                ?: pointReward?.timestamp?.let { TwitchApiHelper.getTimestamp(it, timestampFormat) }

        if (enableTimestamps && timestamp != null) {
            builder.append("$timestamp ")
            builder.setSpan(
                ForegroundColorSpan(Color.parseColor("#999999")),
                imageIndex,
                imageIndex + timestamp.length,
                SPAN_INCLUSIVE_INCLUSIVE
            )
            imageIndex += timestamp.length + 1
        }

        chatMessage.badges?.forEach { chatBadge ->
            val badge =
                channelBadges?.find { it.id == chatBadge.id && it.version == chatBadge.version }
                    ?: globalBadges?.find { it.id == chatBadge.id && it.version == chatBadge.version }

            badge?.url?.let {
                builder.append("  ")
                images.add(Image(it, imageIndex++, imageIndex++, false))
                badgesCount++
            }
        }

        val fullMsg = chatMessage.fullMsg
        val userId = chatMessage.userId
        val userName = chatMessage.userName
        val userNameLength = userName?.length ?: 0
        val userNameEndIndex = imageIndex + userNameLength
        val originalMessage: String
        val userNameWithPostfixLength: Int

        if (chatMessage !is PubSubPointReward && !userName.isNullOrBlank()) {
            builder.append(userName)
            if (!chatMessage.isAction) {
                builder.append(": ")
                originalMessage = "$userName: ${chatMessage.message}"
                userNameWithPostfixLength = userNameLength + 2
            } else {
                builder.append(" ")
                originalMessage = "$userName ${chatMessage.message}"
                userNameWithPostfixLength = userNameLength + 1
            }
        } else {
            if (chatMessage is PubSubPointReward && pointReward?.message.isNullOrBlank()) {
                val string = redeemedNoMsg.format(userName, pointReward?.rewardTitle)
                builder.append("$string ")
                imageIndex += string.length + 1

                val url = when (emoteQuality) {
                    "4" -> pointReward?.rewardImage?.url4
                    "3" -> pointReward?.rewardImage?.url4
                    "2" -> pointReward?.rewardImage?.url2
                    else -> pointReward?.rewardImage?.url1
                }

                url?.let {
                    builder.append("  ")
                    images.add(Image(it, imageIndex++, imageIndex++, false))
                    badgesCount++
                }

                builder.append("${pointReward?.rewardCost}")
                imageIndex += pointReward?.rewardCost?.toString()?.length ?: 0
                originalMessage = "$userName: ${chatMessage.message}"
                userNameWithPostfixLength =
                    string.length + (pointReward?.rewardCost?.toString()?.length ?: 0) + 3

                builder.setSpan(
                    ForegroundColorSpan(Color.parseColor("#999999")),
                    userNameWithPostfixLength - userNameWithPostfixLength,
                    userNameWithPostfixLength,
                    SPAN_INCLUSIVE_INCLUSIVE
                )
            } else {
                originalMessage = "${chatMessage.message}"
                userNameWithPostfixLength = 0
            }
        }

        builder.append(chatMessage.message)

        val color = if (chatMessage is PubSubPointReward) null else
            chatMessage.color.let { userColor ->
                if (userColor == null) {
                    userColors[userName]
                        ?: getRandomColor().also {
                            if (userName != null) {
                                userColors[userName] = it
                            }
                        }
                } else {
                    savedColors[userColor] ?: Color.parseColor(userColor)
                        .also { savedColors[userColor] = it }
                }
            }

        if (color != null && userName != null) {
            builder.setSpan(
                ForegroundColorSpan(color),
                imageIndex,
                userNameEndIndex,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                StyleSpan(Typeface.BOLD),
                imageIndex,
                userNameEndIndex,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        try {
            chatMessage.emotes?.let { emotes ->
                val copy = emotes.map {
                    val realBegin = chatMessage.message?.offsetByCodePoints(0, it.begin) ?: 0
                    val realEnd = if (it.begin == realBegin) {
                        it.end
                    } else {
                        it.end + realBegin - it.begin
                    }
                    TwitchEmote(it.name, realBegin, realEnd)
                }

                imageIndex += userNameWithPostfixLength

                for (e in copy) {
                    val begin = imageIndex + e.begin

                    builder.replace(begin, imageIndex + e.end + 1, ".")
                    builder.setSpan(
                        ForegroundColorSpan(Color.TRANSPARENT),
                        begin,
                        begin + 1,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    val length = e.end - e.begin
                    for (e1 in copy) {
                        if (e.begin < e1.begin) {
                            e1.begin -= length
                            e1.end -= length
                        }
                    }

                    e.end -= length
                }

                copy.forEach {
                    images.add(
                        Image(
                            it.url,
                            imageIndex + it.begin,
                            imageIndex + it.end + 1,
                            true,
                            "image/gif"
                        )
                    )
                }
            }

            val split = builder.split(" ")
            var builderIndex = 0
            var emotesFound = 0
            var wasMentioned = false

            for (value in split) {
                val length = value.length
                val endIndex = builderIndex + length
                var emote = emotes[value]
                val bitsCount = value.takeLastWhile { it.isDigit() }
                val bitsName = value.substringBeforeLast(bitsCount)

                if (bitsCount.isNotEmpty()) {
                    val cheerEmote =
                        cheerEmotes?.findLast { it.name == bitsName && it.minBits <= bitsCount.toInt() }
                    if (cheerEmote != null) {
                        emote = cheerEmote
                        if (emote.color != null) {
                            builder.setSpan(
                                ForegroundColorSpan(Color.parseColor(emote.color)),
                                builderIndex + bitsName.length,
                                endIndex,
                                SPAN_INCLUSIVE_INCLUSIVE
                            )
                        }
                    }
                }

                if (emote == null) {
                    if (!Patterns.WEB_URL.matcher(value).matches()) {
                        if (value.startsWith('@')) {
                            builder.setSpan(
                                StyleSpan(Typeface.BOLD),
                                builderIndex,
                                endIndex,
                                SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        loggedInUser?.let { loggedInUserName ->
                            if (!wasMentioned &&
                                value.contains(loggedInUserName, ignoreCase = true) &&
                                chatMessage.userLogin != loggedInUserName
                            ) {
                                wasMentioned = true
                            }
                        }
                    } else {
                        val url = if (value.startsWith("http")) value else "https://$value"
                        builder.setSpan(
                            URLSpan(url),
                            builderIndex,
                            endIndex,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    builderIndex += length + 1
                } else {
                    for (j in images.lastIndex - emotesFound downTo badgesCount) {
                        val e = images[j]
                        if (e.start > builderIndex) {
                            val remove = if (emote is CheerEmote) {
                                length - 1 - bitsCount.length
                            } else {
                                length - 1
                            }
                            e.start -= remove
                            e.end -= remove
                        }
                    }

                    if (emote is CheerEmote) {
                        builder.replace(builderIndex, builderIndex + bitsName.length, ".")
                    } else {
                        builder.replace(builderIndex, endIndex, ".")
                    }

                    builder.setSpan(
                        ForegroundColorSpan(Color.TRANSPARENT),
                        builderIndex,
                        builderIndex + 1,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    images.add(
                        Image(
                            url = emote.url,
                            start = builderIndex,
                            end = builderIndex + 1,
                            isEmote = true,
                            type = emote.type
                        )
                    )

                    emotesFound++
                    builderIndex += 2

                    if (emote is CheerEmote) {
                        builderIndex += bitsCount.length
                    }
                }
            }

            if (color != null && chatMessage.isAction) {
                builder.setSpan(
                    ForegroundColorSpan(color),
                    if (userName != null) userNameEndIndex + 1 else 0,
                    builder.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            val isFirstMessage = liveMessage?.isFirst == true
            val isRewarded = liveMessage?.rewardId != null && (firstMsgVisibility?.toInt() ?: 0) < 2
            val isNotice = liveMessage?.systemMsg != null || liveMessage?.msgId != null
            val isMention = wasMentioned && userId != null

            val background = when {
                isFirstMessage -> R.color.chatMessageFirst
                isRewarded -> R.color.chatMessageReward
                isNotice -> R.color.chatMessageNotice
                isMention -> R.color.chatMessageMention
                else -> -1
            }

            if (background == -1) holder.textView.background = null
            else holder.textView.setBackgroundResource(background)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.bind(originalMessage, builder, userId, fullMsg)
        loadImages(holder, images, originalMessage, builder, userId, fullMsg)
    }

    override fun getItemCount(): Int = messages?.size ?: 0

    private fun loadImages(
        holder: ViewHolder,
        images: List<Image>,
        originalMessage: CharSequence,
        builder: SpannableStringBuilder,
        userId: String?,
        fullMsg: String?
    ) {
        images.forEach {
            loadCoil(holder, it, originalMessage, builder, userId, fullMsg)
        }
    }

    private fun loadCoil(
        holder: ViewHolder,
        image: Image,
        originalMessage: CharSequence,
        builder: SpannableStringBuilder,
        userId: String?,
        fullMsg: String?
    ) {
        val request = ImageRequest.Builder(context)
            .data(image.url)
            .target(
                onSuccess = { result ->
                    val (width, height) =
                        if (image.isEmote) calculateEmoteSize(result)
                        else badgeSize to badgeSize

                    if (image.isZeroWidth) {
                        result.setBounds(-90, 0, width - 90, height)
                    } else {
                        result.setBounds(0, 0, width, height)
                    }

                    try {
                        builder.setSpan(
                            ImageSpan(result),
                            image.start,
                            image.end,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        if (animateGifs) {
                            (result as? coil.drawable.ScaleDrawable)?.start()
                        }
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }

                    holder.bind(originalMessage, builder, userId, fullMsg)
                },
            )
            .build()

        context.imageLoader.enqueue(request)
    }

    fun addGlobalBadges(list: List<TwitchBadge>) {
        globalBadges = list
    }

    fun addChannelBadges(list: List<TwitchBadge>) {
        channelBadges = list
    }

    fun addCheerEmotes(list: List<CheerEmote>) {
        cheerEmotes = list
    }

    fun addEmotes(list: List<Emote>) {
        emotes.putAll(list.associateBy { it.name })
    }

    fun setUsername(username: String) {
        this.loggedInUser = username
    }

    fun setOnClickListener(listener: OnMessageClickListener?) {
        messageClickListener = listener
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (!animateGifs) return

        (holder.textView.text as? Spannable)
            ?.getSpans<ImageSpan>()
            ?.filterIsInstance<Animatable>()
            ?.forEach { image -> image.start() }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (!animateGifs) return

        (holder.textView.text as? Spannable)
            ?.getSpans<ImageSpan>()
            ?.filterIsInstance<Animatable>()
            ?.forEach { image -> image.stop() }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (!animateGifs) return

        recyclerView.children
            .filterIsInstance<TextView>()
            .forEach { message ->
                (message.text as? Spannable)
                    ?.getSpans<ImageSpan>()
                    ?.forEach {
                        (it.drawable as? coil.drawable.ScaleDrawable)?.stop()
                            ?: (it.drawable as? GifDrawable)?.stop()
                            ?: (it.drawable as? WebpDrawable)?.stop()
                    }
            }
    }

    private fun getRandomColor(): Int =
        if (randomColor) twitchColors[random.nextInt(twitchColors.size)]
        else noColor

    private fun calculateEmoteSize(resource: Drawable): Pair<Int, Int> {
        val widthRatio = resource.intrinsicWidth.toFloat() / resource.intrinsicHeight.toFloat()

        return when {
            widthRatio == 1f -> {
                emoteSize to emoteSize
            }
            widthRatio <= 1.2f -> {
                (emoteSize * widthRatio).toInt() to emoteSize
            }
            else -> {
                (scaledEmoteSize * widthRatio).toInt() to scaledEmoteSize
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView = itemView as TextView

        fun bind(
            originalMessage: CharSequence,
            formattedMessage: SpannableStringBuilder,
            userId: String?,
            fullMsg: String?
        ) {
            textView.apply {
                text = formattedMessage
                movementMethod = LinkMovementMethod.getInstance()
                setOnClickListener {
                    messageClickListener?.onMessageClick(
                        originalMessage = originalMessage,
                        formattedMessage = formattedMessage,
                        userId = userId,
                        fullMsg = fullMsg
                    )
                }
            }
        }
    }
}
