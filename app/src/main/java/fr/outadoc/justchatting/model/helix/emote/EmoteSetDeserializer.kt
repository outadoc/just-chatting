package fr.outadoc.justchatting.model.helix.emote

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import fr.outadoc.justchatting.model.chat.TwitchEmote
import java.lang.reflect.Type

class EmoteSetDeserializer : JsonDeserializer<EmoteSetResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): EmoteSetResponse {
        val emotes = mutableListOf<TwitchEmote>()
        for (i in 0 until json.asJsonObject.getAsJsonArray("data").size()) {
            val emote = json.asJsonObject.getAsJsonArray("data").get(i).asJsonObject

            emotes.add(
                TwitchEmote(
                    id = emote.get("id").asString,
                    name = emote.get("name").asString,
                    supportedFormats = emote.getAsJsonArray("format").map { it.asString },
                    supportedScales = emote.getAsJsonArray("scale").associate {
                        it.asString.toFloat() to it.asString
                    },
                    supportedThemes = emote.getAsJsonArray("theme_mode").map { it.asString },
                    setId = emote.get("emote_set_id").asString,
                    ownerId = emote.get("owner_id").asString
                )
            )
        }

        return EmoteSetResponse(emotes.sortedByDescending { it.setId })
    }
}
