package dev.mme.features.solvers;

import dev.mme.MMEClient;
import dev.mme.MMEConfig;
import dev.mme.core.TextBuilder;
import dev.mme.listener.ClientBossBarListener;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class SpellEstimator implements ClientBossBarListener, ClientTickEvents.EndTick {
    public SpellEstimator() {
        ClientBossBarListener.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
    }
    private static final Map<UUID, ChargingSpell> registeredBars = new HashMap<>();

    public static class Config {
        public boolean enable = true;
        public List<String> prefixes = new ArrayList<>();
        @MMEConfig.MapType(key=String.class, value=Integer.class)
        public Map<String, Integer> knownSpells = new HashMap<>();
        public int getSpellDuration(String text) {
            int duration = knownSpells.getOrDefault(text, -1);
            if (duration == -1 && prefixes.stream().anyMatch(text::startsWith)) {
                return 0;
            }
            return duration;
        }
    }

    public static Config config() {
        return MMEClient.CONFIG.get().solvers.spellEstimator;
    }

    @Override
    public void onBossBar(Type type, UUID uuid) {
        Config config = config();
        if (!config.enable) return;
        ClientBossBar bar = getBar(uuid);
        if (!registeredBars.containsKey(uuid) && config.getSpellDuration(bar.getName().getString()) > -1) return;
        switch (type) {
            case ADD, UPDATE_NAME -> registeredBars.compute(uuid, (ignored, prevVal) -> {
                if (prevVal != null) {
                    prevVal.setInitName(bar.getName());
                    return prevVal;
                }
                return new ChargingSpell(bar);
            });
            case REMOVE -> registeredBars.remove(uuid);
            default -> {}
        }
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (client.world == null) return;
        Config config = config();
        if (!config.enable) return;
        if (client.world.getTime() % 5 == 0) {
            registeredBars.keySet().removeIf(uuid -> getBar(uuid) == null);
        }
        registeredBars.values().forEach(ChargingSpell::onTick);
    }

    private static class ChargingSpell {
        private final ClientBossBar bar;
        private int ticks = 0;
        private boolean isCountdown;
        private String initName;
        private Integer completedAtTicks;
        public ChargingSpell(ClientBossBar bar) {
            this.bar = bar;
            this.isCountdown = Math.round(bar.getPercent()) == 1;
            this.initName = bar.getName().getString();
            this.completedAtTicks = config().getSpellDuration(initName);
        }

        private void setInitName(Text name) {
            this.initName = name.getString();
            this.completedAtTicks = config().getSpellDuration(initName);
            this.isCountdown = Math.round(bar.getPercent()) == 1;
            this.ticks = 0;
        }

        private void onTick() {
            final float percent = bar.getPercent();
            if (percent != 1 && percent != 0) ticks++;
            if (ticks != 0) {
                final double initPercent = isCountdown ? 1 : 0;
                final double dPercent = Math.abs(initPercent - percent) / ticks;
                if (dPercent > 0) {
                    final double ticksToComplete = completedAtTicks <= 0 ? (isCountdown ? (percent / dPercent) : ((1 - percent) / dPercent)) : Math.max(0, completedAtTicks - ticks);
                    bar.setName(new TextBuilder(initName)
                            .append(String.format(Locale.ROOT, " (%s)", toFormattedTimeNormal(ticksToComplete)))
                            .withFormat(Formatting.RED).build());
                }
            }
        }

        public static String toFormattedTimeNormal(double ticks) {
            int mins = (int) ticks / 1200;
            double secs = ticks / 20 - (mins * 60);
            return String.format(Locale.ROOT, "%s%.2fs", mins > 0 ? String.format(Locale.ROOT, "%02dm ", mins) : "", secs);
        }
    }
}