package dev.mme.mixin;

import dev.mme.features.tooltip.TooltipScreenshotter;
import dev.mme.util.ChatUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V", at = @At("HEAD"))
    private void onDrawTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, CallbackInfo ci) {
        var toScreenshot = TooltipScreenshotter.toScreenshot;
        if (toScreenshot != null) {
            TooltipScreenshotter.toScreenshot = null;
            TooltipScreenshotter.screenshotToClipboard(toScreenshot);
            ChatUtils.logInfo("Copied tooltip to clipboard");
        }
    }
}