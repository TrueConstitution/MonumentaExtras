package dev.mme.features.misc;

import dev.mme.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;

public class ItemOverlay {
    private static void drawCooldown(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        ItemCooldownManager cdm = player.getItemCooldownManager();
        ItemCooldownManager.Entry entry = cdm.entries.get(stack.getItem());
        if (entry != null) {
            int start = entry.startTick;
            int end = entry.endTick;
            int now = cdm.tick;
            int tLeft = end - now;
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 200);
            context.drawText(textRenderer, String.valueOf(tLeft/20), x, y, ColorUtils.getPercentageColor(100 - 100f * tLeft / (end-start)).getRgb(), false);
            context.getMatrices().pop();
        }
    }

    public static void onDrawSlot(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
        drawCooldown(context, textRenderer, stack, x, y);
    }
}