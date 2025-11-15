package dev.mme.mixin;

import dev.mme.MMEClient;
import dev.mme.core.Scoreboard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void cancelVanillaScoreboard(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V",
                    ordinal = 0,
                    remap = false
            ),
            slice = @Slice(
                    from= @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V")))
    private void renderCustomScoreboard(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (Scoreboard.config().enable) {
            MMEClient.SCOREBOARD.render(context);
        }
    }
}
