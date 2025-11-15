package dev.mme.mixin;

import dev.mme.listener.KeyListener;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    @Inject(at = @At("HEAD"), method = "onKey", cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int mods, final CallbackInfo info) {
        if (window != client.getWindow().getHandle()) {
            return;
        }
        if (key == -1 || action == 2) {
            return;
        }
        final var keycode = key <= 7 ? InputUtil.Type.MOUSE.createFromCode(key) : InputUtil.Type.KEYSYM.createFromCode(key);
        KeyListener.EVENT.invoker().onKey(keycode, action, info);
    }
}