package dev.mme.mixin;

import dev.mme.listener.ChatListener;
import dev.mme.listener.JoinedPacketListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Shadow
    private Channel channel;

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void mme$onReceivePacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (!channel.isOpen()) {
            return;
        }
        JoinedPacketListener.EVENT.invoker().onJoinedPacket(packet, ci);
        if (packet instanceof GameMessageS2CPacket chatPacket) {
            ChatListener.EVENT.invoker().onChat(chatPacket.content(), ci);
        }
    }
}