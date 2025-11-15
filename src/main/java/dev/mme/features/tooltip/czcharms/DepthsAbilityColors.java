package dev.mme.features.tooltip.czcharms;

import com.google.common.reflect.TypeToken;
import dev.mme.MMEClient;
import dev.mme.core.Config;
import dev.mme.core.MMEAPI;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class DepthsAbilityColors extends Config<DepthsAbilityColors.cfg> {
    public static final DepthsAbilityColors INSTANCE = new DepthsAbilityColors();

    public int getColor(DepthsAbilityInfo info) {
        return config.colors.getOrDefault(info.ability, DepthsTree.getTree(info).color);
    }

    private DepthsAbilityColors() {
        super("cz/colorcoding.json", new cfg("unknown", new TreeMap<>()), new TypeToken<cfg>(){}.getType());
    }

    static class cfg {
        private final String version;
        private final Map<String, Integer> colors;

        cfg(String version, Map<String, Integer> colors) {
            this.version = version;
            this.colors = colors;
        }
    }

    @Override
    protected void init() {
        super.init();
        if (CZCharmAnalysis.config().autoUpdateColorCoding) {
            saveDefaultConfig();
        }
    }

    @Override
    protected void saveDefaultConfig() {
        CompletableFuture.runAsync(() -> {
            try {
                if (update()) super.saveDefaultConfig();
            } catch (IOException e) {
                MMEClient.LOGGER.error(e.getMessage(), e);
            }
        });
    }

    private boolean update() throws IOException {
        MMEClient.LOGGER.info("Fetching latest CZ charms color coding");
        cfg updatedConfig = MMEAPI.fetchGHContent("cz/colorcoding.json", cfg.class);
        if (!updatedConfig.version.equals(config.version)) {
            config = updatedConfig;
            MMEClient.LOGGER.info("Updated colors");
            return true;
        }
        return false;
    }
}