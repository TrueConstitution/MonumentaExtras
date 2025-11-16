package dev.mme.features.strikes.splits.triggers;

import com.google.gson.*;
import dev.mme.util.ChatUtils;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public interface Trigger extends Comparable<Trigger> {
    String name();

    enum MatchMode {
        STARTS_WITH,
        CONTAINS,
        SERVER_MESSAGE_CONTAINS
    }

    boolean triggers(@NotNull Text content, Object... args);

    static boolean contentMatches(@NotNull Text content, Trigger trigger) {
        String str = ChatUtils.stripFormatting(content.getString());
        return switch (trigger.matchMode()) {
            case STARTS_WITH -> str.startsWith(trigger.name());
            case CONTAINS -> str.contains(trigger.name());
            case SERVER_MESSAGE_CONTAINS -> str.contains(trigger.name()) && !str.contains("»");
        };
    }

    default MatchMode matchMode() {
        return MatchMode.SERVER_MESSAGE_CONTAINS;
    }

    default int compareTo(@NotNull Trigger o) {
        return name().compareTo(o.name());
    }

    class Serializer implements JsonSerializer<Trigger>, JsonDeserializer<Trigger> {
        @Override
        public Trigger deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                if (jsonObject == null) {
                    return null;
                } else {
                    String type = JsonHelper.getString(jsonObject, "type");
                    return switch (type) {
                        case "BossBarPercent" -> {
                            String bossBarName = JsonHelper.getString(jsonObject, "bossBarName");
                            float percent = JsonHelper.getFloat(jsonObject, "percent");
                            boolean doExact = jsonObject.has("doExact") && JsonHelper.getBoolean(jsonObject, "doExact");
                            yield new BossBarPercentTrigger(bossBarName, percent, doExact);
                        }
                        case "Regex" -> new RegexTrigger(JsonHelper.getString(jsonObject, "regex"));
                        case "String" -> {
                            String string = JsonHelper.getString(jsonObject, "string");
                            MatchMode matchMode = jsonObject.has("matchMode") ?
                                    MatchMode.valueOf(JsonHelper.getString(jsonObject, "matchMode"))
                                    : MatchMode.SERVER_MESSAGE_CONTAINS;
                            yield new StringTrigger(string, matchMode);
                        }
                        default -> throw new JsonParseException("Unknown trigger type " + type);
                    };
                }
            }
            return null;
        }

        @Override
        public JsonElement serialize(Trigger src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            if (src instanceof BossBarPercentTrigger bossBarPercent) {
                jsonObject.addProperty("type", "BossBarPercent");
                jsonObject.addProperty("bossBarName", bossBarPercent.bossBarName());
                jsonObject.addProperty("percent", bossBarPercent.percent());
                if (bossBarPercent.doExact()) jsonObject.addProperty("doExact", true);
            } else if (src instanceof RegexTrigger regex) {
                jsonObject.addProperty("type", "Regex");
                jsonObject.addProperty("regex", regex.regex());
            } else if (src instanceof StringTrigger string) {
                jsonObject.addProperty("type", "String");
                jsonObject.addProperty("string", string.string());
                if (src.matchMode() != MatchMode.SERVER_MESSAGE_CONTAINS) {
                    jsonObject.addProperty("matchMode", src.matchMode().name());
                }
            } else {
                throw new UnsupportedOperationException("Anonymous Trigger types are not supported");
            }
            return jsonObject;
        }
    }
}