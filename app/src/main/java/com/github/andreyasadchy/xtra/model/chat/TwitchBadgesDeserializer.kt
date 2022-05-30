package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class TwitchBadgesDeserializer : JsonDeserializer<TwitchBadgesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TwitchBadgesResponse {
        val badges = mutableListOf<TwitchBadge>()
        json.asJsonObject.getAsJsonObject("badge_sets").entrySet().forEach { set ->
            set.value.asJsonObject.getAsJsonObject("versions").entrySet().forEach { version ->
                val url = version.value.asJsonObject.get(when (emoteQuality) {"4" -> ("image_url_4x") "3" -> ("image_url_4x") "2" -> ("image_url_2x") else -> ("image_url_1x")}).takeUnless { it?.isJsonNull == true }?.asString ?: version.value.asJsonObject.get("image_url_2x").takeUnless { it?.isJsonNull == true }?.asString ?: version.value.asJsonObject.get("image_url_1x").asString
                badges.add(TwitchBadge(set.key, version.key, url))
            }
        }
        return TwitchBadgesResponse(badges)
    }
}
