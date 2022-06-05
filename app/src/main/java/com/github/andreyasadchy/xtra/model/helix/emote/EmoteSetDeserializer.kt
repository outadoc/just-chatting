package com.github.andreyasadchy.xtra.model.helix.emote

import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.ui.view.chat.ChatView
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.lang.reflect.Type

class EmoteSetDeserializer : JsonDeserializer<EmoteSetResponse> {

    companion object {
        private const val BASE_EMOTE_URL = "https://static-cdn.jtvnw.net/emoticons/v2"
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): EmoteSetResponse {
        val emotes = mutableListOf<TwitchEmote>()
        for (i in 0 until json.asJsonObject.getAsJsonArray("data").size()) {
            val emote = json.asJsonObject.getAsJsonArray("data").get(i).asJsonObject

            val preferredScale = when (ChatView.emoteQuality) {
                "4" -> "4.0"
                "3" -> "3.0"
                "2" -> "2.0"
                else -> "1.0"
            }

            val supportedFormats = emote.getAsJsonArray("format").map { it.asString }
            val supportedScales = emote.getAsJsonArray("scale").map { it.asString }

            val isAnimated = supportedFormats.any { it == "animated" }

            emotes.add(
                TwitchEmote(
                    name = emote.get("name").asString,
                    type = if (isAnimated) "image/gif" else "image/png",
                    url = createUrlForEmote(
                        id = emote.get("id").asString,
                        format = if (isAnimated) "animated" else "static",
                        scale = supportedScales.firstOrNull { it == preferredScale }
                            ?: supportedScales.last()
                            ?: "1.0"
                    ),
                    setId = emote.get("emote_set_id").asString,
                    ownerId = emote.get("owner_id").asString
                )
            )
        }

        return EmoteSetResponse(emotes.sortedByDescending { it.setId })
    }

    private fun createUrlForEmote(
        id: String,
        format: String,
        theme: String = "light",
        scale: String
    ): String {
        return BASE_EMOTE_URL.toHttpUrl()
            .newBuilder()
            .addPathSegment(id)
            .addPathSegment(format)
            .addPathSegment(theme)
            .addPathSegment(scale)
            .build()
            .toString()
    }
}
