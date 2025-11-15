package dev.mme.mixin;

import dev.mme.listener.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
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
        } else if (packet instanceof TitleS2CPacket titlePacket) {
            TitleListener.EVENT.invoker().onTitle(titlePacket.getTitle(), ci);
        } else if (packet instanceof SubtitleS2CPacket subtitlePacket) {
            SubtitleListener.EVENT.invoker().onSubtitle(subtitlePacket.getSubtitle(), ci);
        } else if (packet instanceof OverlayMessageS2CPacket actionbarPacket) {
            ActionbarListener.EVENT.invoker().onActionbar(actionbarPacket.getMessage(), ci);
        }
    }
}