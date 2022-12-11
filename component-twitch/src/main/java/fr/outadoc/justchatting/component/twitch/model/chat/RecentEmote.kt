package fr.outadoc.justchatting.component.twitch.model.chat

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

    companion object {
        const val MAX_SIZE = 50
    }

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        return url
    }
}
