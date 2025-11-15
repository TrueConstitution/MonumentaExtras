package dev.mme.core;

import com.google.gson.*;
import net.minecraft.text.TextColor;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class TextColorSerializer implements JsonDeserializer<TextColor>, JsonSerializer<TextColor> {
    public TextColorSerializer() {
    }

    @Override
    public TextColor deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject == null) {
                return null;
            } else {
                int rgb = JsonHelper.getInt(jsonObject, "rgb");
                return TextColor.fromRgb(rgb);
            }
        } else {
            return null;
        }
    }

    @Nullable
    public JsonElement serialize(TextColor color, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("rgb", color.getRgb());
        return jsonObject;
    }
}