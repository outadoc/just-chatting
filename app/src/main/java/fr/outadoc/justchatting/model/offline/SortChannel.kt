package fr.outadoc.justchatting.model.offline

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "sort_channel")
data class SortChannel(
    @PrimaryKey
    val id: String,
    var saveSort: Boolean? = null,
    var videoSort: String? = null,
    var videoType: String? = null,
    var clipPeriod: String? = null
) : Parcelable
