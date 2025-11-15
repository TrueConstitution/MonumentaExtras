package dev.mme.listener;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface TitleListener {
    Event<TitleListener> EVENT = EventFactory.createArrayBacked(TitleListener.class, listeners -> (Text message, CallbackInfo ci) -> {
        for (TitleListener listener : listeners) {
            listener.onTitle(message, ci);
        }
    });
    void onTitle(Text message, CallbackInfo ci);
}
