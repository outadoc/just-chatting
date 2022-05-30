package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.ui.view.chat.animateGifs
import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class CheerEmotesDeserializer : JsonDeserializer<CheerEmotesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CheerEmotesResponse {
        val emotes = mutableListOf<CheerEmote>()
        for (i in json.asJsonObject.get("data").asJsonArray) {
            val name = i.asJsonObject.get("prefix")
            for (tier in i.asJsonObject.get("tiers").asJsonArray) {
                val minBits = tier.asJsonObject.get("min_bits")
                val color = tier.asJsonObject.get("color")
                val images = tier.asJsonObject.get("images").asJsonObject.get("dark").asJsonObject
                val animated = animateGifs && images.toString().contains("animated")
                val urls = images.get(if (animated) "animated" else "static").asJsonObject
                val url = urls.get(when (emoteQuality) {"4" -> ("4") "3" -> ("3") "2" -> ("2") else -> ("1")}).takeUnless { it?.isJsonNull == true }?.asString ?: urls.get("3").takeUnless { it?.isJsonNull == true }?.asString ?: urls.get("2").takeUnless { it?.isJsonNull == true }?.asString ?: urls.get("1").asString
                emotes.add(CheerEmote(name.asString, minBits.asInt, color.asString, if (animated) "image/gif" else "image/png", url))
            }
        }
        return CheerEmotesResponse(emotes)
    }
}
