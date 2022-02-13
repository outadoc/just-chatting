package com.github.andreyasadchy.xtra.model.gql.tag

import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class TagStreamDataDeserializer : JsonDeserializer<TagStreamDataResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TagStreamDataResponse {
        val data = mutableListOf<Tag>()
        val dataJson = json.asJsonObject.getAsJsonObject("data").getAsJsonArray("topTags")
        dataJson.forEach {
            val obj = it.asJsonObject
            data.add(Tag(
                    id = if (!(obj.get("id").isJsonNull)) { obj.getAsJsonPrimitive("id").asString } else null,
                    name = if (!(obj.get("localizedName").isJsonNull)) { obj.getAsJsonPrimitive("localizedName").asString } else null,
                    scope = if (!(obj.get("scope").isJsonNull)) { obj.getAsJsonPrimitive("scope").asString } else null,
                )
            )
        }
        return TagStreamDataResponse(data)
    }
}
