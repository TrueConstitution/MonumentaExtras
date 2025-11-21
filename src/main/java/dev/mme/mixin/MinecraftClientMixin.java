package dev.mme.mixin;

import dev.mme.features.cz.CZCharmDB;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(at = @At("TAIL"), method = "setScreen")
    public void afterOpenScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof HandledScreen<?>) {
            CZCharmDB.INSTANCE.parseCharms((HandledScreen<?>) screen);
        }
    }
}