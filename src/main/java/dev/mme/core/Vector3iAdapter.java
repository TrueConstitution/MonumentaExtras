package dev.mme.core;

import com.google.gson.*;
import org.joml.Vector3i;

import java.lang.reflect.Type;

public class Vector3iAdapter implements JsonSerializer<Vector3i>, JsonDeserializer<Vector3i> {
    @Override
    public JsonElement serialize(Vector3i src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray arr = new JsonArray();
        arr.add(src.x);
        arr.add(src.y);
        arr.add(src.z);
        return arr;
    }

    @Override
    public Vector3i deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (json.isJsonArray()) {
            JsonArray arr = json.getAsJsonArray();
            if (arr.size() != 3) {
                throw new JsonParseException("Vector3i array must have exactly 3 elements");
            }
            return new Vector3i(
                    arr.get(0).getAsInt(),
                    arr.get(1).getAsInt(),
                    arr.get(2).getAsInt()
            );
        }

        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();

            if (!obj.has("x") || !obj.has("y") || !obj.has("z")) {
                throw new JsonParseException("Vector3i object must contain x, y, z");
            }

            return new Vector3i(
                    obj.get("x").getAsInt(),
                    obj.get("y").getAsInt(),
                    obj.get("z").getAsInt()
            );
        }

        throw new JsonParseException("Invalid Vector3i format; expected array or object");
    }
}