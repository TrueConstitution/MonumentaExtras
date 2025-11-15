package dev.mme.listener;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;

public interface InteractBlockListener {
    Event<InteractBlockListener> EVENT = EventFactory.createArrayBacked(InteractBlockListener.class, listeners -> (BlockHitResult blockHitResult, BlockState blockState) -> {
        for (InteractBlockListener listener : listeners) {
            listener.onUse(blockHitResult, blockState);
        }
    });
    void onUse(BlockHitResult blockHitResult, BlockState blockState);
}