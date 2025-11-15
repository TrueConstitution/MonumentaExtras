package dev.mme;

import dev.mme.core.Config;
import dev.mme.features.solvers.SpellEstimator;
import dev.mme.features.tooltip.czcharms.CZCharmAnalysis;
import dev.mme.features.tooltip.czcharms.CZCharmDB;
import dev.mme.util.Reflections;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMEClient implements ClientModInitializer {
    public static ConfigHolder<MMEConfig> CONFIG;
    public static Logger LOGGER = LoggerFactory.getLogger("mmev2");
    @Override
    public void onInitializeClient() {
        CONFIG = MMEConfig.register();
        new SpellEstimator();
        new CZCharmAnalysis();
        new CZCharmDB();
        Reflections.DEFAULT.getSubTypesOf(Config.class);
    }
}
