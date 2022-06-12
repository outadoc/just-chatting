package com.github.andreyasadchy.xtra.model.chat

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class StvEmotesDeserializer : JsonDeserializer<StvEmotesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): StvEmotesResponse {
        val emotes = mutableListOf<StvEmote>()
        for (i in 0 until json.asJsonArray.size()) {
            val emote = json.asJsonArray.get(i).asJsonObject
            val urls = emote.getAsJsonArray("urls")
            val visibility = emote.getAsJsonArray("visibility_simple")
            val isZeroWidth = visibility.toString().contains("ZERO_WIDTH")

            emotes.add(
                StvEmote(
                    name = emote.get("name").asString,
                    urls = urls.toMap(),
                    isZeroWidth = isZeroWidth
                )
            )
        }
        return StvEmotesResponse(emotes)
    }

    private fun JsonArray.toMap(): Map<Float, String> {
        return associate { element ->
            val array = element.asJsonArray
            val density = array.get(0).asString.toFloat()
            val url = array.get(1).asString
            density to url
        }
    }
}
