package dev.mme;

import dev.mme.core.Config;
import dev.mme.core.Scoreboard;
import dev.mme.features.solvers.SpellEstimator;
import dev.mme.features.solvers.content.skr.SKRScrollSolver;
import dev.mme.features.strikes.StrikesManager;
import dev.mme.features.strikes.splits.Splits;
import dev.mme.features.tooltip.TooltipScreenshotter;
import dev.mme.features.tooltip.czcharms.CZCharmAnalysis;
import dev.mme.features.tooltip.czcharms.CZCharmDB;
import dev.mme.listener.ActionbarListener;
import dev.mme.util.FS;
import dev.mme.util.Reflections;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMEClient implements ClientModInitializer {
    public static ConfigHolder<MMEConfig> CONFIG;
    public static Logger LOGGER = LoggerFactory.getLogger("mmev2");
    public static Scoreboard SCOREBOARD;
    @Override
    public void onInitializeClient() {
        CONFIG = MMEConfig.register();
        if (!FS.exists("")) {FS.mkdirs("");}
        new SpellEstimator();
        new CZCharmAnalysis();
        new SKRScrollSolver();
        new MMECommand();
        SCOREBOARD = new Scoreboard();
        CZCharmDB.class.getName();
        Splits.class.getName();
        TooltipScreenshotter.class.getName();
        ActionbarListener.EVENT.register(StrikesManager::onActionbar);
        Reflections.DEFAULT.getSubTypesOf(Config.class);
        ClientLifecycleEvents.CLIENT_STOPPING
                .register(client -> Reflections.getInstances(Reflections.DEFAULT.getSubTypesOf(Config.class))
                .forEach(c -> Reflections.invokeMethod(c, "saveJson")));
    }
}
