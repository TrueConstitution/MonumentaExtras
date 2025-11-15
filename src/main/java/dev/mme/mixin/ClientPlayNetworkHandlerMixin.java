package dev.mme.mixin;

import dev.mme.listener.ClientBossBarListener;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(at = @At("TAIL"), method = "onBossBar")
    public void onBossBar(BossBarS2CPacket packet, CallbackInfo info) {
        packet.accept(new ClientBossBarListener.Consumer());
    }
}