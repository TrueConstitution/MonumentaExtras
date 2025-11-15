package dev.mme.features.strikes.splits.triggers;

import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record BossBarPercentTrigger(String bossBarName, float percent, boolean doExact) implements Trigger {
    public BossBarPercentTrigger(String bossBarName, float percent) {
        this(bossBarName, percent, false);
    }

    @Override
    public String name() {
        return bossBarName;
    }

    /**
     * @param args 0 instanceof {@link ClientBossBar}
     */
    @Override
    public boolean triggers(@NotNull Text content, Object... args) {
        if (Trigger.contentMatches(content, this) && (args[0] instanceof ClientBossBar bar)) {
            float barPercent = bar.getPercent() * 100;
            if (doExact) {
                if (((int) percent) == percent) return ((int) barPercent) == percent;
                return Math.abs(barPercent - percent) < 0.01;
            } else {
                return barPercent <= percent;
            }
        }
        return false;
    }
}