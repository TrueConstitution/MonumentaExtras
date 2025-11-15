package dev.mme.access;

import net.minecraft.client.gui.hud.ClientBossBar;

import java.util.Map;
import java.util.UUID;

public interface IBossBarHud {
    Map<UUID, ClientBossBar> mme$getBossbars();
}
