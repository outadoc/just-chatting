package com.github.andreyasadchy.xtra.model.gql.tag

import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class TagSearchGameStreamDataDeserializer : JsonDeserializer<TagSearchGameStreamDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TagSearchGameStreamDataResponse {
        val data = mutableListOf<Tag>()
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonArray("searchLiveTags")
        dataJson.forEach {
            val obj = it.asJsonObject
            data.add(Tag(
                    id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else null,
                    name = if (!(obj.get("localizedName").isJsonNull)) { obj.getAsJsonPrimitive("localizedName").asString } else null,
                    scope = "ALL"
                )
            )
        }
        return TagSearchGameStreamDataResponse(data)
    }
}
