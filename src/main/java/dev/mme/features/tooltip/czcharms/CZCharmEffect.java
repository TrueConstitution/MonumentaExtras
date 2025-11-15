package dev.mme.features.tooltip.czcharms;

import com.google.gson.*;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public record CZCharmEffect(double roll, DepthsAbilityInfo effect, CZCharmRarity rarity) {
    public CZCharmEffect upgrade() {
        return new CZCharmEffect(roll, effect, rarity.upgrade());
    }

    public double value() {
        double rawBaseValue = effect.rarityValue(rarity);
        double delta = effect.variance * (2.0 * roll - 1.0);
        double value = rawBaseValue;
        if (effect.variance != 0.0) {
            value = rawBaseValue + delta;
            if (rawBaseValue >= 5.0) {
                value = Math.round(value);
            } else {
                value = Math.round(value * 100d) / 100d;
            }
        }
        if (rarity.mIsNegative) {
            value = -value;
        }
        return value;
    }

    public boolean canUpgrade(CZCharmRarity charmRarity, int budget) {
        return rarity != CZCharmRarity.LEGENDARY && charmRarity.ordinal() > rarity.ordinal() && budget + rarity.upgradeCost() >= 0;
    }

    public static class Serializer implements JsonDeserializer<CZCharmEffect>, JsonSerializer<CZCharmEffect> {
        @Override
        public CZCharmEffect deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject == null) {
                    return null;
                } else {
                    double roll = JsonHelper.getDouble(jsonObject, "roll");
                    String name = JsonHelper.getString(jsonObject, "name");
                    CZCharmRarity rarity = CZCharmRarity.valueOf(JsonHelper.getString(jsonObject, "rarity"));
                    return new CZCharmEffect(roll, DepthsAbilityInfo.get(name), rarity);
                }
            } else {
                return null;
            }
        }

        @Nullable
        public JsonElement serialize(CZCharmEffect effect, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("roll", effect.roll);
            if (effect.effect != null) {
                jsonObject.addProperty("name", effect.effect.effectName);
            } else {
                jsonObject.addProperty("name", "unknown");
            }
            jsonObject.addProperty("rarity", effect.rarity.name());
            return jsonObject;
        }
    }
}