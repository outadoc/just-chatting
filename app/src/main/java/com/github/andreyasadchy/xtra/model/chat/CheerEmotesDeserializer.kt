package com.github.andreyasadchy.xtra.model.chat

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class CheerEmotesDeserializer : JsonDeserializer<CheerEmotesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CheerEmotesResponse {
        val emotes = mutableListOf<CheerEmote>()
        for (i in json.asJsonObject.get("data").asJsonArray) {
            val name = i.asJsonObject.get("prefix")
            for (tier in i.asJsonObject.get("tiers").asJsonArray) {
                val minBits = tier.asJsonObject.get("min_bits")
                val color = tier.asJsonObject.get("color")
                val images = tier.asJsonObject.get("images").asJsonObject.get("dark").asJsonObject

                val staticUrls = images.get("static").takeIf { it.isJsonObject }?.asJsonObject
                val animatedUrls = images.get("animated").takeIf { it.isJsonObject }?.asJsonObject

                emotes.add(
                    CheerEmote(
                        name = name.asString,
                        minBits = minBits.asInt,
                        color = color.asString,
                        staticUrls = staticUrls?.toMap().orEmpty(),
                        animatedUrls = animatedUrls?.toMap().orEmpty()
                    )
                )
            }
        }

        return CheerEmotesResponse(emotes)
    }

    private fun JsonObject.toMap(): Map<Float, String> {
        return keySet().associate { key ->
            key.toFloat() to get(key).asString
        }
    }
}
