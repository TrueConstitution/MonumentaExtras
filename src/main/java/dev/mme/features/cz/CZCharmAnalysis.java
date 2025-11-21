package dev.mme.features.cz;

import dev.mme.MMEClient;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.*;

public class CZCharmAnalysis implements ItemTooltipCallback {
    public CZCharmAnalysis() {
        ItemTooltipCallback.EVENT.register(this);
    }

    public static class Config {
        @ConfigEntry.Gui.PrefixText
        public boolean enable = true;
        public boolean showBudget = true;
        public DisplayMode displayMode = DisplayMode.Tabular;
        public boolean showUUID = false;
        public boolean autoUpdateColorCoding = true;
    }
    public enum DisplayMode {
        Compact,
        Tabular
    }

    public static Config config() {
        return MMEClient.CONFIG.get().cz.charmanalysis;
    }

    @Override
    public void getTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip) {
        if (!config().enable || !CZCharm.isZenithCharm(stack.getOrCreateNbt())) return;
        CZCharm charm = CZCharm.parseNBT(stack.getOrCreateNbt());
        tooltip.set(0, charm.displayName());
        tooltip.subList(1, tooltip.size()).clear();
        tooltip.addAll(charm.lore());
    }
}