package com.github.andreyasadchy.xtra.model.offline

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "videos")
data class OfflineVideo(
    val url: String,
    @ColumnInfo(name = "source_url")
    val sourceUrl: String? = null,
    @ColumnInfo(name = "source_start_position")
    val sourceStartPosition: Long? = null,
    val name: String? = null,
    @ColumnInfo(name = "channel_id")
    val channelId: String? = null,
    @ColumnInfo(name = "channel_login")
    var channelLogin: String? = null,
    @ColumnInfo(name = "channel_name")
    var channelName: String? = null,
    @ColumnInfo(name = "channel_logo")
    var channelLogo: String? = null,
    val thumbnail: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
    val duration: Long? = null,
    @ColumnInfo(name = "upload_date")
    val uploadDate: Long? = null,
    @ColumnInfo(name = "download_date")
    val downloadDate: Long? = null,
    @ColumnInfo(name = "last_watch_position")
    var lastWatchPosition: Long? = null,
    var progress: Int,
    @ColumnInfo(name = "max_progress")
    val maxProgress: Int,
    var status: Int = STATUS_PENDING,
    val type: String? = null,
    val videoId: String? = null) : Parcelable {

    @IgnoredOnParcel
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @IgnoredOnParcel
    @ColumnInfo(name = "is_vod")
    var vod = url.endsWith(".m3u8")

    companion object {
        const val STATUS_PENDING = 0
        const val STATUS_DOWNLOADING = 1
        const val STATUS_DOWNLOADED = 2
    }
}
