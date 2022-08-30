package fr.outadoc.justchatting.model.chat

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class BttvFfzDeserializer : JsonDeserializer<BttvFfzResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BttvFfzResponse {
        val emotes = mutableListOf<FfzEmote>()
        for (i in 0 until json.asJsonArray.size()) {
            val emote = json.asJsonArray.get(i).asJsonObject
            val urls = emote.getAsJsonObject("images")

            emotes.add(
                FfzEmote(
                    name = emote.get("code").asString,
                    urls = urls.toMap()
                )
            )
        }
        return BttvFfzResponse(emotes)
    }

    private fun JsonObject.toMap(): Map<Float, String> {
        return keySet()
            .mapNotNull { key ->
                val url = get(key).takeUnless { it.isJsonNull }?.asString
                if (url != null) key.trimEnd('x').toFloat() to url else null
            }
            .toMap()
    }
}
