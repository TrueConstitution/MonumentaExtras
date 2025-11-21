package dev.mme.features.solvers.content.skr;

import dev.mme.MMEClient;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class SKRSolvers {
    public static class Config {
        @ConfigEntry.Gui.PrefixText
        public boolean enable = true;
        public boolean useLocalRiddleDataOverride = false;
    }
    public static Config config() {
        return MMEClient.CONFIG.get().solvers.skr;
    }
}
