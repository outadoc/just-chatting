package fr.outadoc.justchatting.model.chat

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
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