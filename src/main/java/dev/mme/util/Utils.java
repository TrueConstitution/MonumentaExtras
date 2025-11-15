package dev.mme.util;

import dev.mme.MMEClient;
import dev.mme.mixin.access.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class Utils {
    public static String logError(Throwable exception, @Nullable String message) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        if (message != null) {
            printWriter.println(message);
        }
        exception.printStackTrace(printWriter);
        MMEClient.LOGGER.error(writer.toString());
        return writer.toString();
    }

    public static Pattern verifyPattern(String s) throws PatternSyntaxException {
        return s == null || s.isEmpty() ? null : Pattern.compile(s);
    }

    public static Optional<ItemStack> getHoveredItem() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen instanceof HandledScreen<?> screen) {
            final var slot = ((HandledScreenAccessor) screen).getFocusedSlot();
            if (slot != null) return Optional.of(slot.getStack());
        }
        if (mc.currentScreen instanceof ChatScreen) {
            double x = mc.mouse.getX() * (double) mc.getWindow().getScaledWidth() / (double) mc.getWindow().getWidth();
            double y = mc.mouse.getY() * (double) mc.getWindow().getScaledHeight() / (double) mc.getWindow().getHeight();
            final var style = mc.inGameHud.getChatHud().getTextStyleAt(x, y);
            final var hoverEvent = style == null ? null : style.getHoverEvent();
            final var contents = hoverEvent == null ? null : hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
            if (contents != null) return Optional.of(contents.asStack());
        }
        return Optional.empty();
    }
}
