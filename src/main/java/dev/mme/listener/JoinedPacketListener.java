package dev.mme.listener;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface JoinedPacketListener {
    Event<JoinedPacketListener> EVENT = EventFactory.createArrayBacked(JoinedPacketListener.class, listeners -> (Packet<?> packet, CallbackInfo ci) -> {
        for (JoinedPacketListener listener : listeners) {
            listener.onJoinedPacket(packet, ci);
        }
    });
    void onJoinedPacket(Packet<?> packet, CallbackInfo ci);
}