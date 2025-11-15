package dev.mme.mixin;

import dev.mme.access.IBossBarHud;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.*;

import java.util.Map;
import java.util.UUID;

@Mixin(BossBarHud.class)
@Implements(@Interface(iface = IBossBarHud.class, prefix = "soft$"))
public abstract class BossBarHudMixin implements IBossBarHud {
    @Shadow
    @Final
    Map<UUID, ClientBossBar> bossBars;

    public Map<UUID, ClientBossBar> mme$getBossbars() {
        return this.bossBars;
    }
}