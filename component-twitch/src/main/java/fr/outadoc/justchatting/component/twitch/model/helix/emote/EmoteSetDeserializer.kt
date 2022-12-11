package fr.outadoc.justchatting.component.twitch.model.helix.emote

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import fr.outadoc.justchatting.component.twitch.model.chat.TwitchEmote
import java.lang.reflect.Type

class EmoteSetDeserializer : JsonDeserializer<EmoteSetResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): EmoteSetResponse {
        val urlTemplate = json.asJsonObject.get("template").asString
        val emotes = json.asJsonObject.getAsJsonArray("data")
            .map { it.asJsonObject }
            .map { emote ->
                TwitchEmote(
                    id = emote.get("id").asString,
                    name = emote.get("name").asString,
                    supportedFormats = emote.getAsJsonArray("format").map { it.asString },
                    supportedScales = emote.getAsJsonArray("scale").map { it.asString },
                    supportedThemes = emote.getAsJsonArray("theme_mode").map { it.asString },
                    setId = emote.get("emote_set_id").asString,
                    ownerId = emote.get("owner_id").asString,
                    urlTemplate = urlTemplate
                )
            }
            .sortedByDescending { it.setId }

        return EmoteSetResponse(emotes)
    }
}
