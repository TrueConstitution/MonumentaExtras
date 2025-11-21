package dev.mme.features.cz;

import dev.mme.MMEClient;
import dev.mme.core.TextBuilder;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import java.util.*;

public class CZMenuFix implements ItemTooltipCallback {
    public CZMenuFix() {
        ItemTooltipCallback.EVENT.register(this);
    }

    public static class Config {
        @ConfigEntry.Gui.PrefixText
        public boolean enable = true;
    }

    public static Config config() {
        return MMEClient.CONFIG.get().cz.menufix;
    }

    private static final int START_OF_CHARMS = 45;

    @Override
    public void getTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        if (!config().enable || lines.isEmpty() || !lines.get(0).getString().equals("Charm Effect Summary")) return;
        if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> screen) {
            ScreenHandler handler = screen.getScreenHandler();
            // 7 charm slots
            Map<DepthsAbilityInfo, Double> effectMap = new HashMap<>();
            for (int i = 0; i < 7; i++) {
                NbtCompound nbt = handler.getSlot(START_OF_CHARMS+i).getStack().getOrCreateNbt();
                if (!CZCharm.isZenithCharm(nbt)) break;
                for (CZCharmEffect effect : CZCharm.parseNBT(nbt).effects()) {
                    effectMap.merge(effect.effect(), effect.value(), Double::sum);
                }
            }

            List<Text> effectLines = effectMap
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(info -> info.effectName)))
                    .map(entry -> {
                        DepthsAbilityInfo effect = entry.getKey();
                        double stat = entry.getValue();
                        double cappedValue = effect.effectCap >= 0 ? Math.min(effect.effectCap, stat) : Math.max(effect.effectCap, stat);
                        StringBuilder builder = new StringBuilder();
                        builder.append(effect.effectName);
                        if (effect.isPercent) builder.append(" %");
                        builder.append(String.format(Locale.ROOT, " : %.2f", cappedValue));
                        if (effect.isPercent) builder.append("%");
                        if (cappedValue == effect.effectCap) {
                            builder.append(" (MAX");
                            if (stat != effect.effectCap) {
                                builder.append(String.format(Locale.ROOT, "; %.2f", Math.abs(effect.effectCap - stat)));
                                if (effect.isPercent) {
                                    builder.append("%");
                                }
                            }
                            builder.append(" overflow)");
                            return new TextBuilder(builder.toString()).withColor(0xe49b20).build();
                        }
                        boolean isDebuff = effect.rarityValues[effect.maxRarity-1] < 0 != stat < 0;
                        return new TextBuilder(builder.toString()).withColor(isDebuff ? 0xD02E28 : 0x4AC2E5).build();
            }).toList();

            for (int cutoff = lines.size() - 3; cutoff >= 1; cutoff--) {
                if (lines.get(cutoff).getString().endsWith("Charm Power Used")) {
                    lines.subList(2, cutoff - 1).clear();
                    lines.addAll(2, effectLines);
                    if (!lines.get(1).getString().equals("These Charms are currently disabled!")) {
                        lines.remove(1);
                    }
                    break;
                }
            }
        }

    }
}
