package fr.outadoc.justchatting.component.twitch.adapters

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import fr.outadoc.justchatting.component.twitch.parser.model.StvEmote
import fr.outadoc.justchatting.component.twitch.parser.model.StvEmotesResponse
import java.lang.reflect.Type

class StvEmotesDeserializer : JsonDeserializer<StvEmotesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): StvEmotesResponse {
        val emotes = json.asJsonArray
            .map { it.asJsonObject }
            .map { emote ->
                val urls = emote.getAsJsonArray("urls")
                val visibility = emote.getAsJsonArray("visibility_simple")
                val isZeroWidth = visibility.any { it.asString == "ZERO_WIDTH" }

                StvEmote(
                    name = emote.get("name").asString,
                    urls = urls.toMap(),
                    isZeroWidth = isZeroWidth
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
