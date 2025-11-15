package dev.mme.features.solvers.content.skr;

import dev.mme.MMEClient;

public class SKRSolvers {
    public static class Config {
        public boolean enable = true;
    }
    public static Config config() {
        return MMEClient.CONFIG.get().solvers.skr;
    }
}
