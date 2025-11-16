package dev.mme.core;

import dev.mme.MMEClient;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class Scoreboard implements ClientTickEvents.EndTick {
    public Scoreboard() {
        ClientTickEvents.END_CLIENT_TICK.register(this);
    }
    public static class Config {
        public boolean enable = true;
        public boolean useTransparentBackground = false;
        @ConfigEntry.ColorPicker
        public int fallbackColor = 0xFFFFFF;
    }

    public static Config config() {
        return MMEClient.CONFIG.get().scoreboard;
    }
    private long animateTick = 0;
    private final List<Text> lines = new ArrayList<>();
    private Supplier<List<Text>> contentSupplier;

    public void onEndTick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) return;
        animateTick++;
        lines.clear();
        lines.add(new TextBuilder(" " + new SimpleDateFormat("MM/dd/yy").format(Calendar.getInstance(Locale.US).getTime()))
                .withFormat(Formatting.GRAY)
                .append(" " + ShardInfo.INSTANCE.getShard())
                .withFormat(Formatting.DARK_GRAY).build());
        lines.add(Text.literal(""));
        if (contentSupplier != null) {
            lines.addAll(contentSupplier.get());
        } else {
            lines.add(new TextBuilder(" ⏣ ").withFormat(Formatting.GRAY).append(ShardInfo.INSTANCE.getLocation()).withFormat(Formatting.RED).build());
        }
        lines.add(Text.literal(""));
        lines.add(new TextBuilder(" server.playmonumenta.com ").withFormat(Formatting.YELLOW).build());
    }

    public void setContentSupplier(Supplier<List<Text>> contentSupplier) {
        this.contentSupplier = contentSupplier;
    }

    public void render(DrawContext context) {
        if (context == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        int fontHeight = client.textRenderer.fontHeight;
        String title = getMonumenta();
        Config config = config();
        int width = Math.max(client.textRenderer.getWidth(title), lines.stream().mapToInt(client.textRenderer::getWidth).max().orElse(0));
        int height = fontHeight * (lines.size() + 1);
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int x = sw - width - 2;
        int y = (sh - height) / 2;
        if (!config.useTransparentBackground) {
            context.fill(x - 2, y - 2, sw, y + height + 2, client.options.getTextBackgroundColor(120/255f));
        }

        context.drawCenteredTextWithShadow(client.textRenderer, title, x+width/2, y, 0xFFFF55);

        int currY = y + fontHeight;
        for (Text line : lines) {
            int color = line.getStyle().getColor() == null ? config.fallbackColor : line.getStyle().getColor().getRgb();
            context.drawTextWithShadow(client.textRenderer, line, x, currY, color);
            currY += fontHeight;
        }
    }

    private String getMonumenta() {
        if (animateTick < 10) {
            return "§lMONUMENTA";
        } else if (animateTick < 12) {
            return "§6§lM§e§lONUMENTA";
        } else if (animateTick < 14) {
            return "§f§lM§6§lO§e§lNUMENTA";
        } else if (animateTick < 16) {
            return "§f§lMO§6§lN§e§lUMENTA";
        } else if (animateTick < 18) {
            return "§f§lMON§6§lU§e§lMENTA";
        } else if (animateTick < 20) {
            return "§f§lMONU§6§lM§e§lENTA";
        } else if (animateTick < 22) {
            return "§f§lMONUM§6§lE§e§lNTA";
        } else if (animateTick < 24) {
            return "§f§lMONUME§6§lN§e§lTA";
        } else if (animateTick < 26) {
            return "§f§lMONUMEN§6§lT§e§lA";
        } else if (animateTick < 28) {
            return "§f§lMONUMENT§6§lA";
        } else if (animateTick < 35) {
            return "§f§lMONUMENTA";
        } else if (animateTick < 42) {
            return "§lMONUMENTA";
        } else if (animateTick < 49) {
            return "§f§lMONUMENTA";
        } else {
            if (animateTick >= 100) {
                animateTick = 0;
            }
            return "§lMONUMENTA";
        }
    }
}