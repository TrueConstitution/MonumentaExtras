package dev.mme.listener;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface KeyListener {
    Event<KeyListener> EVENT = EventFactory.createArrayBacked(KeyListener.class, listeners -> (InputUtil.Key key, int action, CallbackInfo ci) -> {
        for (KeyListener listener : listeners) {
            listener.onKey(key, action, ci);
        }
    });
    void onKey(InputUtil.Key key, int action, CallbackInfo ci);
}
