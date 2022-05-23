package com.github.andreyasadchy.xtra.ui.common

import android.graphics.Color
import android.graphics.Typeface
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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.GlideApp
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.*
import com.github.andreyasadchy.xtra.ui.view.chat.animateGifs
import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import java.util.*
import kotlin.collections.set

class ChatAdapter(
    private val fragment: Fragment,
    private val emoteSize: Int,
    private val badgeSize: Int,
    private val randomColor: Boolean,
    private val boldNames: Boolean,
    private val enableZeroWidth: Boolean,
    private val enableTimestamps: Boolean,
    private val timestampFormat: String?,
    private val firstMsgVisibility: String?,
    private val firstChatMsg: String,
    private val rewardChatMsg: String,
    private val redeemedChatMsg: String,
    private val redeemedNoMsg: String,
    private val imageLibrary: String?) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    var messages: MutableList<ChatMessage>? = null
        set(value) {
            val oldSize = field?.size ?: 0
            if (oldSize > 0) {
                notifyItemRangeRemoved(0, oldSize)
            }
            field = value
        }
    private val twitchColors = intArrayOf(-65536, -16776961, -16744448, -5103070, -32944, -6632142, -47872, -13726889, -2448096, -2987746, -10510688, -14774017, -38476, -7722014, -16711809)
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

    private var messageClickListener: ((CharSequence, CharSequence, String?, String?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_list_item, parent, false))
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
            val msgId = liveMessage?.msgId?.let { TwitchApiHelper.getMessageIdString(it) ?: liveMessage.msgId }
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
                4 -> pointReward?.rewardImage?.url4
                3 -> pointReward?.rewardImage?.url4
                2 -> pointReward?.rewardImage?.url2
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
        val timestamp = liveMessage?.timestamp?.let { TwitchApiHelper.getTimestamp(it, timestampFormat) } ?: pointReward?.timestamp?.let { TwitchApiHelper.getTimestamp(it, timestampFormat) }
        if (enableTimestamps && timestamp != null) {
            builder.append("$timestamp ")
            builder.setSpan(ForegroundColorSpan(Color.parseColor("#999999")), imageIndex, imageIndex + timestamp.length, SPAN_INCLUSIVE_INCLUSIVE)
            imageIndex += timestamp.length + 1
        }
        chatMessage.badges?.forEach { chatBadge ->
            val badge = channelBadges?.find { it.id == chatBadge.id && it.version == chatBadge.version } ?: globalBadges?.find { it.id == chatBadge.id && it.version == chatBadge.version }
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
                    4 -> pointReward?.rewardImage?.url4
                    3 -> pointReward?.rewardImage?.url4
                    2 -> pointReward?.rewardImage?.url2
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
                userNameWithPostfixLength = string.length + (pointReward?.rewardCost?.toString()?.length ?: 0) + 3
                builder.setSpan(ForegroundColorSpan(Color.parseColor("#999999")), userNameWithPostfixLength - userNameWithPostfixLength, userNameWithPostfixLength, SPAN_INCLUSIVE_INCLUSIVE)
            } else {
                originalMessage = "${chatMessage.message}"
                userNameWithPostfixLength = 0
            }
        }
        builder.append(chatMessage.message)
        val color = if (chatMessage is PubSubPointReward) null else
            chatMessage.color.let { userColor ->
                if (userColor == null) {
                    userColors[userName] ?: getRandomColor().also { if (userName != null) userColors[userName] = it }
                } else {
                    savedColors[userColor] ?: Color.parseColor(userColor).also { savedColors[userColor] = it }
                }
            }
        if (color != null && userName != null) {
            builder.setSpan(ForegroundColorSpan(color), imageIndex, userNameEndIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(StyleSpan(if (boldNames) Typeface.BOLD else Typeface.NORMAL), imageIndex, userNameEndIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
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
                    builder.setSpan(ForegroundColorSpan(Color.TRANSPARENT), begin, begin + 1, SPAN_EXCLUSIVE_EXCLUSIVE)
                    val length = e.end - e.begin
                    for (e1 in copy) {
                        if (e.begin < e1.begin) {
                            e1.begin -= length
                            e1.end -= length
                        }
                    }
                    e.end -= length
                }
                copy.forEach { images.add(Image(it.url, imageIndex + it.begin, imageIndex + it.end + 1, true, "image/gif")) }
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
                    val cheerEmote = cheerEmotes?.findLast { it.name == bitsName && it.minBits <= bitsCount.toInt() }
                    if (cheerEmote != null) {
                        emote = cheerEmote
                        if (emote.color != null) {
                            builder.setSpan(ForegroundColorSpan(Color.parseColor(emote.color)), builderIndex + bitsName.length, endIndex, SPAN_INCLUSIVE_INCLUSIVE)
                        }
                    }
                }
                if (emote == null) {
                    if (!Patterns.WEB_URL.matcher(value).matches()) {
                        if (value.startsWith('@')) {
                            builder.setSpan(StyleSpan(if (boldNames) Typeface.BOLD else Typeface.NORMAL), builderIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        loggedInUser?.let {
                            if (!wasMentioned && value.contains(it, true) && chatMessage.userLogin != it) {
                                wasMentioned = true
                            }
                        }
                    } else {
                        val url = if (value.startsWith("http")) value else "https://$value"
                        builder.setSpan(URLSpan(url), builderIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
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
                    builder.setSpan(ForegroundColorSpan(Color.TRANSPARENT), builderIndex, builderIndex + 1, SPAN_EXCLUSIVE_EXCLUSIVE)
                    images.add(Image(emote.url, builderIndex, builderIndex + 1, true, emote.type, emote.zeroWidth))
                    emotesFound++
                    builderIndex += 2
                    if (emote is CheerEmote) {
                        builderIndex += bitsCount.length
                    }
                }
            }
            if (color != null && chatMessage.isAction) {
                builder.setSpan(ForegroundColorSpan(color), if (userName != null) userNameEndIndex + 1 else 0, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            when {
                liveMessage?.isFirst == true && (firstMsgVisibility?.toInt() ?: 0) < 2 -> holder.textView.setBackgroundResource(R.color.chatMessageFirst)
                liveMessage?.rewardId != null && (firstMsgVisibility?.toInt() ?: 0) < 2 -> holder.textView.setBackgroundResource(R.color.chatMessageReward)
                liveMessage?.systemMsg != null || liveMessage?.msgId != null -> holder.textView.setBackgroundResource(R.color.chatMessageNotice)
                wasMentioned && userId != null -> holder.textView.setBackgroundResource(R.color.chatMessageMention)
                else -> holder.textView.background = null
            }
        } catch (e: Exception) {
//            Crashlytics.logException(e)
        }
        holder.bind(originalMessage, builder, userId, fullMsg)
        loadImages(holder, images, originalMessage, builder, userId, fullMsg)
    }

    override fun getItemCount(): Int = messages?.size ?: 0

    private fun loadImages(holder: ViewHolder, images: List<Image>, originalMessage: CharSequence, builder: SpannableStringBuilder, userId: String?, fullMsg: String?) {
        images.forEach {
            when (imageLibrary) {
                "0" -> loadCoil(holder, it, originalMessage, builder, userId, fullMsg)
                "1" -> {
                    if (it.type == "image/webp") {
                        if (animateGifs) {
                            loadWebp(holder, it, originalMessage, builder, userId, fullMsg)
                        } else {
                            loadDrawable(holder, it, originalMessage, builder, userId, fullMsg)
                        }
                    } else {
                        loadCoil(holder, it, originalMessage, builder, userId, fullMsg)
                    }
                }
                else -> {
                    if (it.type == "image/webp" && animateGifs) {
                        loadWebp(holder, it, originalMessage, builder, userId, fullMsg)
                    } else {
                        if (it.type == "image/gif" && animateGifs) {
                            loadGif(holder, it, originalMessage, builder, userId, fullMsg)
                        } else {
                            loadDrawable(holder, it, originalMessage, builder, userId, fullMsg)
                        }
                    }
                }
            }
        }
    }

    private fun loadCoil(holder: ViewHolder, image: Image, originalMessage: CharSequence, builder: SpannableStringBuilder, userId: String?, fullMsg: String?) {
        val request = ImageRequest.Builder(fragment.requireContext())
            .data(image.url)
            .target(
                onSuccess = { result ->
                    val width: Int
                    val height: Int
                    if (image.isEmote) {
                        val size = calculateEmoteSize(result)
                        width = size.first
                        height = size.second
                    } else {
                        width = badgeSize
                        height = badgeSize
                    }
                    if (image.zerowidth && enableZeroWidth) {
                        result.setBounds(-90, 0, width - 90, height)
                    } else {
                        result.setBounds(0, 0, width, height)
                    }
                    try {
                        builder.setSpan(ImageSpan(result), image.start, image.end, SPAN_EXCLUSIVE_EXCLUSIVE)
                        if (animateGifs) {
                            (result as? coil.drawable.ScaleDrawable)?.start()
                        }
                    } catch (e: IndexOutOfBoundsException) {
                    }
                    holder.bind(originalMessage, builder, userId, fullMsg)
                },
            )
            .build()
        fragment.requireContext().imageLoader.enqueue(request)
    }

    private fun loadWebp(holder: ViewHolder, image: Image, originalMessage: CharSequence, builder: SpannableStringBuilder, userId: String?, fullMsg: String?) {
        GlideApp.with(fragment)
            .asWebp()
            .load(image.url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(object : CustomTarget<WebpDrawable>() {
                override fun onResourceReady(resource: WebpDrawable, transition: Transition<in WebpDrawable>?) {
                    resource.apply {
                        val size = calculateEmoteSize(this)
                        if (image.zerowidth && enableZeroWidth) {
                            setBounds(-90, 0, size.first - 90, size.second)
                        } else {
                            setBounds(0, 0, size.first, size.second)
                        }
                        loopCount = WebpDrawable.LOOP_FOREVER
                        callback = object : Drawable.Callback {
                            override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                                holder.textView.removeCallbacks(what)
                            }

                            override fun invalidateDrawable(who: Drawable) {
                                holder.textView.invalidate()
                            }

                            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                                holder.textView.postDelayed(what, `when`)
                            }
                        }
                        start()
                    }
                    try {
                        builder.setSpan(ImageSpan(resource), image.start, image.end, SPAN_EXCLUSIVE_EXCLUSIVE)
                    } catch (e: IndexOutOfBoundsException) {
                    }
                    holder.bind(originalMessage, builder, userId, fullMsg)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    loadDrawable(holder, image, originalMessage, builder, userId, fullMsg)
                }
            })
    }

    private fun loadGif(holder: ViewHolder, image: Image, originalMessage: CharSequence, builder: SpannableStringBuilder, userId: String?, fullMsg: String?) {
        GlideApp.with(fragment)
            .asGif()
            .load(image.url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(object : CustomTarget<GifDrawable>() {
                override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
                    resource.apply {
                        val size = calculateEmoteSize(this)
                        if (image.zerowidth && enableZeroWidth) {
                            setBounds(-90, 0, size.first - 90, size.second)
                        } else {
                            setBounds(0, 0, size.first, size.second)
                        }
                        setLoopCount(GifDrawable.LOOP_FOREVER)
                        callback = object : Drawable.Callback {
                            override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                                holder.textView.removeCallbacks(what)
                            }

                            override fun invalidateDrawable(who: Drawable) {
                                holder.textView.invalidate()
                            }

                            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                                holder.textView.postDelayed(what, `when`)
                            }
                        }
                        start()
                    }
                    try {
                        builder.setSpan(ImageSpan(resource), image.start, image.end, SPAN_EXCLUSIVE_EXCLUSIVE)
                    } catch (e: IndexOutOfBoundsException) {
                    }
                    holder.bind(originalMessage, builder, userId, fullMsg)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    loadDrawable(holder, image, originalMessage, builder, userId, fullMsg)
                }
            })
    }

    private fun loadDrawable(holder: ViewHolder, image: Image, originalMessage: CharSequence, builder: SpannableStringBuilder, userId: String?, fullMsg: String?) {
        GlideApp.with(fragment)
            .load(image.url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    val width: Int
                    val height: Int
                    if (image.isEmote) {
                        val size = calculateEmoteSize(resource)
                        width = size.first
                        height = size.second
                    } else {
                        width = badgeSize
                        height = badgeSize
                    }
                    if (image.zerowidth && enableZeroWidth) {
                        resource.setBounds(-90, 0, width - 90, height)
                    } else {
                        resource.setBounds(0, 0, width, height)
                    }
                    try {
                        builder.setSpan(ImageSpan(resource), image.start, image.end, SPAN_EXCLUSIVE_EXCLUSIVE)
                    } catch (e: IndexOutOfBoundsException) {
                    }
                    holder.bind(originalMessage, builder, userId, fullMsg)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
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

    fun setOnClickListener(listener: (CharSequence, CharSequence, String?, String?) -> Unit) {
        messageClickListener = listener
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (animateGifs) {
            (holder.textView.text as? Spannable)?.getSpans<ImageSpan>()?.forEach {
                (it.drawable as? coil.drawable.ScaleDrawable)?.start() ?:
                (it.drawable as? GifDrawable)?.start() ?:
                (it.drawable as? WebpDrawable)?.start()
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (animateGifs) {
            (holder.textView.text as? Spannable)?.getSpans<ImageSpan>()?.forEach {
                (it.drawable as? coil.drawable.ScaleDrawable)?.stop() ?:
                (it.drawable as? GifDrawable)?.stop() ?:
                (it.drawable as? WebpDrawable)?.stop()
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        val childCount = recyclerView.childCount
        if (animateGifs) {
            for (i in 0 until childCount) {
                ((recyclerView.getChildAt(i) as TextView).text as? Spannable)?.getSpans<ImageSpan>()?.forEach {
                    (it.drawable as? coil.drawable.ScaleDrawable)?.stop() ?:
                    (it.drawable as? GifDrawable)?.stop() ?:
                    (it.drawable as? WebpDrawable)?.stop()
                }
            }
        }
        super.onDetachedFromRecyclerView(recyclerView)
    }

    private fun getRandomColor(): Int =
        if (randomColor) {
            twitchColors[random.nextInt(twitchColors.size)]
        } else {
            noColor
        }

    private fun calculateEmoteSize(resource: Drawable): Pair<Int, Int> {
        val widthRatio = resource.intrinsicWidth.toFloat() / resource.intrinsicHeight.toFloat()
        val width: Int
        val height: Int
        when {
            widthRatio == 1f -> {
                width = emoteSize
                height = emoteSize
            }
            widthRatio <= 1.2f -> {
                width = (emoteSize * widthRatio).toInt()
                height = emoteSize
            }
            else -> {
                width = (scaledEmoteSize * widthRatio).toInt()
                height = scaledEmoteSize
            }
        }
        return width to height
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView = itemView as TextView

        fun bind(originalMessage: CharSequence, formattedMessage: SpannableStringBuilder, userId: String?, fullMsg: String?) {
            textView.apply {
                text = formattedMessage
                movementMethod = LinkMovementMethod.getInstance()
                setOnClickListener { messageClickListener?.invoke(originalMessage, formattedMessage, userId, fullMsg) }
            }
        }
    }
}
