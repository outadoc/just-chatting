package com.github.andreyasadchy.xtra.model.chat

import com.github.andreyasadchy.xtra.ui.view.chat.stvQuality
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class StvRoomDeserializer : JsonDeserializer<StvEmotesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): StvEmotesResponse {
        val emotes = mutableListOf<StvEmote>()
        for (i in 0 until json.asJsonArray.size()) {
            val emote = json.asJsonArray.get(i).asJsonObject
            val urls = emote.getAsJsonArray("urls")
            val quality = urls.get(when (stvQuality) {4 -> 3 3 -> 2 2 -> 1 else -> 0}).asJsonArray
            val url = quality.get(1)
            emotes.add(StvEmote(emote.get("name").asString, emote.get("mime").asString, url.asString))
        }
        return StvEmotesResponse(emotes)
    }
}
