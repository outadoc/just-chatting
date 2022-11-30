package fr.outadoc.justchatting.model.chat

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import fr.outadoc.justchatting.utils.core.asStringOrNull
import java.lang.reflect.Type

class BttvFfzDeserializer : JsonDeserializer<BttvFfzResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BttvFfzResponse {
        val emotes = json.asJsonArray
            .map { it.asJsonObject }
            .map { emote ->
                FfzEmote(
                    name = emote.get("code").asString,
                    urls = emote.getAsJsonObject("images")
                        .entrySet()
                        .mapNotNull { it.value.asStringOrNull?.let { value -> it.key to value } }
                        .toMap()
                )
            }

        return BttvFfzResponse(emotes)
    }
}
