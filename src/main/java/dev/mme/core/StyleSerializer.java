package dev.mme.core;

import com.google.gson.*;
import dev.mme.mixin.access.StyleAccessor;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class StyleSerializer implements JsonSerializer<Style> {
    public StyleSerializer() {
    }

    @Nullable
    public JsonElement serialize(Style style, Type type, JsonSerializationContext jsonSerializationContext) {
        if (style.isEmpty()) {
            return null;
        } else {
            JsonObject jsonObject = new JsonObject();
            if (((StyleAccessor) style).mme$getBold() != null) {
                jsonObject.addProperty("bold", ((StyleAccessor) style).mme$getBold());
            }

            if (((StyleAccessor) style).mme$getItalic() != null) {
                jsonObject.addProperty("italic", ((StyleAccessor) style).mme$getItalic());
            }

            if (((StyleAccessor) style).mme$getUnderlined() != null) {
                jsonObject.addProperty("underlined", ((StyleAccessor) style).mme$getUnderlined());
            }

            if (((StyleAccessor) style).mme$getStrikethrough() != null) {
                jsonObject.addProperty("strikethrough", ((StyleAccessor) style).mme$getStrikethrough());
            }

            if (((StyleAccessor) style).mme$getObfuscated() != null) {
                jsonObject.addProperty("obfuscated", ((StyleAccessor) style).mme$getObfuscated());
            }


            if (style.getColor() != null) {
                jsonObject.add("color", jsonSerializationContext.serialize(style.getColor()));
            }

            if (((StyleAccessor) style).mme$getFont() != null) {
                jsonObject.addProperty("font", ((StyleAccessor) style).mme$getFont().toString());
            }

            if (style.getInsertion() != null) {
                jsonObject.add("insertion", jsonSerializationContext.serialize(style.getInsertion()));
            }

            if (style.getClickEvent() != null) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("action", style.getClickEvent().getAction().asString());
                jsonObject2.addProperty("value", style.getClickEvent().getValue());
                jsonObject.add("clickEvent", jsonObject2);
            }

            if (style.getHoverEvent() != null) {
                jsonObject.add("hoverEvent", jsonSerializationContext.serialize(style.getHoverEvent()));
            }

            if (style.getFont() != null && style.getFont() != Style.DEFAULT_FONT_ID) {
                jsonObject.addProperty("font", style.getFont().toString());
            }

            return jsonObject;
        }
    }
}