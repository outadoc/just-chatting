package com.github.andreyasadchy.xtra.ui.saved.bookmarks

import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.model.offline.Bookmark
import com.github.andreyasadchy.xtra.model.offline.VodBookmarkIgnoredUser
import com.github.andreyasadchy.xtra.ui.common.BaseListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosFragment
import com.github.andreyasadchy.xtra.util.*
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.date
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.duration
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.gameName
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.options
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.thumbnail
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.title
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.type
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.userImage
import kotlinx.android.synthetic.main.fragment_downloads_list_item.view.username
import kotlinx.android.synthetic.main.fragment_videos_list_item.view.*

class BookmarksAdapter(
    private val fragment: Fragment,
    private val clickListener: BaseVideosFragment.OnVideoSelectedListener,
    private val channelClickListener: OnChannelSelectedListener,
    private val gameClickListener: GamesFragment.OnGameSelectedListener,
    private val showDownloadDialog: (Video) -> Unit,
    private val vodIgnoreUser: (String) -> Unit,
    private val deleteVideo: (Bookmark) -> Unit) : BaseListAdapter<Bookmark>(
    object : DiffUtil.ItemCallback<Bookmark>() {
        override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return false //bug, oldItem and newItem are sometimes the same
        }
    }) {

    override val layoutId: Int = R.layout.fragment_videos_list_item

    private var positions: Map<Long, Long>? = null

    fun setVideoPositions(positions: Map<Long, Long>) {
        this.positions = positions
        if (!currentList.isNullOrEmpty()) {
            notifyDataSetChanged()
        }
    }

    private var ignored: List<VodBookmarkIgnoredUser>? = null

    fun setIgnoredUsers(list: List<VodBookmarkIgnoredUser>) {
        this.ignored = list
        if (!currentList.isNullOrEmpty()) {
            notifyDataSetChanged()
        }
    }

    private var users: List<User>? = null

    fun setLoadedUsers(list: List<User>) {
        this.users = list
        if (!currentList.isNullOrEmpty()) {
            notifyDataSetChanged()
        }
    }

    private var videos: List<Video>? = null

    fun setLoadedVideos(list: List<Video>) {
        this.videos = list
        if (!currentList.isNullOrEmpty()) {
            notifyDataSetChanged()
        }
    }

    override fun bind(item: Bookmark, view: View) {
        val channelListener: (View) -> Unit = { channelClickListener.viewChannel(item.userId, item.userLogin, item.userName, item.userLogo, updateLocal = true) }
        val gameListener: (View) -> Unit = { gameClickListener.openGame(item.gameId, item.gameName) }
        with(view) {
            val loadedVideo = videos?.find { it.id == item.id }
            val getDuration = if (loadedVideo?.duration != null) {
                TwitchApiHelper.getDuration(loadedVideo.duration)
            } else {
                item.duration?.let { TwitchApiHelper.getDuration(it) }
            }
            val position = positions?.get(item.id.toLong())
            val ignore = ignored?.find { it.user_id == item.userId } != null
            val loadedUser = users?.find { it.id == item.userId }
            val userType = loadedUser?.type ?: loadedUser?.broadcaster_type
            setOnClickListener { clickListener.startVideo(loadedVideo ?: Video(
                id = item.id,
                user_id = item.userId,
                user_login = item.userLogin,
                user_name = item.userName,
                profileImageURL = item.userLogo,
                gameId = item.gameId,
                gameName = item.gameName,
                title = item.title,
                createdAt = item.createdAt,
                thumbnail_url = item.thumbnail,
                type = item.type,
                duration = item.duration,
            ), position?.toDouble()) }
            setOnLongClickListener { deleteVideo(item); true }
            thumbnail.loadImage(fragment, loadedVideo?.thumbnail ?: item.thumbnail, diskCacheStrategy = DiskCacheStrategy.AUTOMATIC)
            if (item.createdAt != null) {
                val text = TwitchApiHelper.formatTimeString(context, item.createdAt)
                if (text != null) {
                    date.visible()
                    date.text = text
                } else {
                    date.gone()
                }
            } else {
                date.gone()
            }
            if (item.type?.lowercase() == "archive" && userType != null && item.createdAt != null && context.prefs().getBoolean(C.UI_BOOKMARK_TIME_LEFT, true) && !ignore) {
                val time = TwitchApiHelper.getVodTimeLeft(context, item.createdAt,
                    when (userType.lowercase()) {
                        "" -> 14
                        "affiliate" -> 14
                        else -> 60
                    })
                if (!time.isNullOrBlank()) {
                    views.visible()
                    views.text = context.getString(R.string.vod_time_left, time)
                } else {
                    views.gone()
                }
            } else {
                views.gone()
            }
            if (getDuration != null) {
                duration.visible()
                duration.text = DateUtils.formatElapsedTime(getDuration)
            } else {
                duration.gone()
            }
            if (item.type != null) {
                val text = TwitchApiHelper.getType(context, item.type)
                if (text != null) {
                    type.visible()
                    type.text = text
                } else {
                    type.gone()
                }
            } else {
                type.gone()
            }
            if (item.userLogo != null)  {
                userImage.visible()
                userImage.loadImage(fragment, item.userLogo, circle = true)
                userImage.setOnClickListener(channelListener)
            } else {
                userImage.gone()
            }
            if (item.userName != null)  {
                username.visible()
                username.text = item.userName
                username.setOnClickListener(channelListener)
            } else {
                username.gone()
            }
            if (position != null && getDuration != null && getDuration > 0L) {
                progressBar.progress = (position / (getDuration * 10)).toInt()
                progressBar.visible()
            } else {
                progressBar.gone()
            }
            if (item.title != null)  {
                title.visible()
                title.text = item.title.trim()
            } else {
                title.gone()
            }
            if (item.gameName != null)  {
                gameName.visible()
                gameName.text = item.gameName
                gameName.setOnClickListener(gameListener)
            } else {
                gameName.gone()
            }
            options.setOnClickListener { it ->
                PopupMenu(context, it).apply {
                    inflate(R.menu.offline_item)
                    if (item.id.isNotBlank()) {
                        menu.findItem(R.id.download).isVisible = true
                    }
                    if (item.type?.lowercase() == "archive" && item.userId != null && context.prefs().getBoolean(C.UI_BOOKMARK_TIME_LEFT, true)) {
                        menu.findItem(R.id.vodIgnore).isVisible = true
                        if (ignore) {
                            menu.findItem(R.id.vodIgnore).title = context.getString(R.string.vod_remove_ignore)
                        } else {
                            menu.findItem(R.id.vodIgnore).title = context.getString(R.string.vod_ignore_user)
                        }
                    }
                    setOnMenuItemClickListener {
                        when(it.itemId) {
                            R.id.delete -> deleteVideo(item)
                            R.id.download -> showDownloadDialog(Video(
                                id = item.id,
                                user_id = item.userId,
                                user_login = item.userLogin,
                                user_name = item.userName,
                                profileImageURL = item.userLogo,
                                gameId = item.gameId,
                                gameName = item.gameName,
                                title = item.title,
                                createdAt = item.createdAt,
                                thumbnail_url = item.thumbnail,
                                type = item.type,
                                duration = item.duration,
                            ))
                            R.id.vodIgnore -> item.userId?.let { id -> vodIgnoreUser(id) }
                            else -> menu.close()
                        }
                        true
                    }
                    show()
                }
            }
        }
    }
}