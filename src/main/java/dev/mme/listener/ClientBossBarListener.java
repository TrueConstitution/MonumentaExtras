package dev.mme.listener;

import dev.mme.access.IBossBarHud;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.text.Text;

import java.util.UUID;

public interface ClientBossBarListener {
    Event<ClientBossBarListener> EVENT = EventFactory.createArrayBacked(ClientBossBarListener.class, listeners -> (Type type, UUID uuid) -> {
        for (ClientBossBarListener listener : listeners) {
            listener.onBossBar(type, uuid);
        }
    });
    void onBossBar(Type type, UUID uuid);

    default ClientBossBar getBar(UUID uuid) {
        return ((IBossBarHud) MinecraftClient.getInstance().inGameHud.getBossBarHud()).mme$getBossbars().get(uuid);
    }

    enum Type {
        ADD,
        REMOVE,
        UPDATE_PERCENT,
        UPDATE_NAME,
        UPDATE_STYLE,
        UPDATE_PROPERTIES
    }

    class Consumer implements BossBarS2CPacket.Consumer {
        public void add(UUID uuid, Text name, float percent, BossBar.Color color, BossBar.Style style, boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
            EVENT.invoker().onBossBar(Type.ADD, uuid);
        }

        public void remove(UUID uuid) {
            EVENT.invoker().onBossBar(Type.REMOVE, uuid);
        }

        public void updateProgress(UUID uuid, float percent) {
            EVENT.invoker().onBossBar(Type.UPDATE_PERCENT, uuid);
        }

        public void updateName(UUID uuid, Text name) {
            EVENT.invoker().onBossBar(Type.UPDATE_NAME, uuid);
        }

        public void updateStyle(UUID id, BossBar.Color color, BossBar.Style style) {
            EVENT.invoker().onBossBar(Type.UPDATE_STYLE, id);
        }

        public void updateProperties(UUID uuid, boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
            EVENT.invoker().onBossBar(Type.UPDATE_PROPERTIES, uuid);
        }

    }
}
