package fr.outadoc.justchatting.component.twitch.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_emotes")
class RecentEmote(
    @PrimaryKey
    override val name: String,
    val url: String,
    @ColumnInfo(name = "used_at")
    val usedAt: Long
) : Emote() {

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        return url
    }
}
