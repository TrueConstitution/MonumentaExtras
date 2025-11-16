package dev.mme.mixin;

import dev.mme.features.misc.ItemOverlay;
import dev.mme.features.tooltip.TooltipScreenshotter;
import dev.mme.util.ChatUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.item.ItemStack;
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

    @Inject(method="drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value="INVOKE", target="Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER)
    )
    private void onDrawSlot(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        ItemOverlay.onDrawSlot((DrawContext) (Object) this, textRenderer, stack, x, y);
    }
}