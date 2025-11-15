package dev.mme.listener;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public interface ItemUseListener {
    Event<ItemUseListener> EVENT = EventFactory.createArrayBacked(ItemUseListener.class, listeners -> (World world, PlayerEntity user, Hand hand) -> {
        for (ItemUseListener listener : listeners) {
            listener.onUse(world, user, hand);
        }
    });
    void onUse(World world, PlayerEntity user, Hand hand);
}
