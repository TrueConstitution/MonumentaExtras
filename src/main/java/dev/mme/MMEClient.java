package dev.mme;

import dev.mme.feature.SpellEstimator;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;

public class MMEClient implements ClientModInitializer {
    public static ConfigHolder<MMEConfig> CONFIG;
    @Override
    public void onInitializeClient() {
        CONFIG = MMEConfig.register();
        SpellEstimator estimator = new SpellEstimator();
    }
}
