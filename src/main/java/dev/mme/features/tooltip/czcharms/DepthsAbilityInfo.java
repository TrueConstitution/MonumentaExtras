package dev.mme.features.tooltip.czcharms;

import com.google.common.reflect.TypeToken;
import com.google.gson.annotations.Expose;
import dev.mme.core.Config;
import dev.mme.core.MMEAPI;
import dev.mme.core.TextBuilder;
import dev.mme.util.FS;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DepthsAbilityInfo {
    public static class API extends Config<List<DepthsAbilityInfo>> {
        public static final API INSTANCE = new API();

        private API() {
            super("zenith_charm_effects.json", null, new TypeToken<List<DepthsAbilityInfo>>() {}.getType());
        }

        @Override
        protected void loadJson() throws IOException {
            super.loadJson();
            data.clear();
            for (DepthsAbilityInfo info : config) {
                data.put(info.effectName, info);
            }
        }

        @Override
        protected void init() {
            try {
                FS.write(CONFIG_PATH, MMEAPI.fetchData("https://api.playmonumenta.com", "/zenith_charm_effects"));
                this.loadJson();
                CZCharmDB.DB.init();
            } catch (IOException ignored) {}
        }
    }
    @Expose
    private static final Map<String, DepthsAbilityInfo> data = new HashMap<>();

    public String effectName;
    public String ability;
    public boolean isOnlyPositive;
    public boolean isPercent;
    public double variance;
    public double effectCap;
    public DepthsTree tree;
    public double[] rarityValues;

    public String modifierName() {
        return effectName.replace(ability, "").strip();
    }

    public double getMaxRoll(int rarity) {
        return rarityValues[rarity - 1] + (effectCap >= 0 ? variance : -variance);
    }

    public static List<String> getEffectNames() {
        return data.values().stream().map(v -> v.effectName).toList();
    }

    public static DepthsAbilityInfo get(String fx) {
        if (fx.endsWith("%")) fx = fx.substring(0, fx.length() - 1);
        return data.get(fx);
    }

    public Text getAbilityDisplayName() {
        return new TextBuilder(ability).withColor(DepthsAbilityColors.INSTANCE.getColor(this)).build();
    }

    public double rarityValue(CZCharmRarity rarity) {
        return this.rarityValues[rarity.mRarity-1];
    }
}