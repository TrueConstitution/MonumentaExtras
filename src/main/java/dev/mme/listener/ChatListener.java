package dev.mme.listener;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface ChatListener {
    Event<ChatListener> EVENT = EventFactory.createArrayBacked(ChatListener.class, listeners -> (Text message, CallbackInfo ci) -> {
        for (ChatListener listener : listeners) {
            listener.onChat(message, ci);
        }
    });
    void onChat(Text message, CallbackInfo ci);
}
