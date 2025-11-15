package dev.mme.util;

import dev.mme.MMEClient;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import static net.minecraft.util.math.MathHelper.lerp;

public class ColorUtils {
    private static final NavigableMap<Float, Formatting> LERP_MAP = new TreeMap<>(Map.of(
            0f,
            Formatting.RED,
            70f,
            Formatting.YELLOW,
            90f,
            Formatting.GREEN,
            100f,
            Formatting.AQUA));
    private static final NavigableMap<Float, Formatting> FLAT_MAP = new TreeMap<>(Map.of(
            30f,
            Formatting.RED,
            80f,
            Formatting.YELLOW,
            96f,
            Formatting.GREEN,
            Float.MAX_VALUE,
            Formatting.AQUA));
    
    public static TextColor getPercentageColor(float percentage) {
        if (!MMEClient.CONFIG.get().misc.lerpColor) return getFlatPercentageColor(percentage);
        Map.Entry<Float, Formatting> lowerEntry = LERP_MAP.floorEntry(percentage);
        Map.Entry<Float, Formatting> higherEntry = LERP_MAP.ceilingEntry(percentage);

        // Boundary conditions
        if (lowerEntry == null) {
            return TextColor.fromFormatting(higherEntry.getValue());
        } else if (higherEntry == null) {
            return TextColor.fromFormatting(lowerEntry.getValue());
        }

        if (Objects.equals(lowerEntry.getKey(), higherEntry.getKey())) {
            return TextColor.fromFormatting(lowerEntry.getValue());
        }

        float t = (percentage - lowerEntry.getKey()) / (higherEntry.getKey() - lowerEntry.getKey());

        int lowerColor = Objects.requireNonNull(lowerEntry.getValue().getColorValue());
        int higherColor = Objects.requireNonNull(higherEntry.getValue().getColorValue());

        int r = (int) lerp((lowerColor >> 16) & 0xff, (higherColor >> 16) & 0xff, t);
        int g = (int) lerp((lowerColor >> 8) & 0xff, (higherColor >> 8) & 0xff, t);
        int b = (int) lerp(lowerColor & 0xff, higherColor & 0xff, t);

        return TextColor.fromRgb((r << 16) | (g << 8) | b);
    }

    public static TextColor getFlatPercentageColor(float percentage) {
        return TextColor.fromFormatting(FLAT_MAP.higherEntry(percentage).getValue());
    }
}
