package dev.mme.listener;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface ActionbarListener {
    Event<ActionbarListener> EVENT = EventFactory.createArrayBacked(ActionbarListener.class, listeners -> (Text message, CallbackInfo ci) -> {
        for (ActionbarListener listener : listeners) {
            listener.onActionbar(message, ci);
        }
    });
    void onActionbar(Text message, CallbackInfo ci);
}
