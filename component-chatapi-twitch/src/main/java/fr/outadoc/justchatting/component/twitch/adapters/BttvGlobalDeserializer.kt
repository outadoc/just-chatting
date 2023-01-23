package fr.outadoc.justchatting.component.twitch.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import fr.outadoc.justchatting.component.twitch.model.BttvEmote
import fr.outadoc.justchatting.component.twitch.model.BttvGlobalResponse
import java.lang.reflect.Type

class BttvGlobalDeserializer : JsonDeserializer<BttvGlobalResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BttvGlobalResponse {
        val emotes = json.asJsonArray
            .map { it.asJsonObject }
            .map { emote ->
                BttvEmote(
                    id = emote.get("id").asString,
                    name = emote.get("code").asString
                )
            }

        return BttvGlobalResponse(emotes)
    }
}
