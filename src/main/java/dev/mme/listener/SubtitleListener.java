package dev.mme.listener;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface SubtitleListener {
    Event<SubtitleListener> EVENT = EventFactory.createArrayBacked(SubtitleListener.class, listeners -> (Text message, CallbackInfo ci) -> {
        for (SubtitleListener listener : listeners) {
            listener.onSubtitle(message, ci);
        }
    });
    void onSubtitle(Text message, CallbackInfo ci);
}
