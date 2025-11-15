package dev.mme.features.strikes;

import dev.mme.util.ChatUtils;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StrikesManager {
    private static int totalChests = 0;
    private static int currentChests = 0;
    private static final Pattern CHESTS_PATTERN = Pattern.compile("(\\d+) total chests added to lootroom\\..+");
    public static void overrideTotalChests(int totalChests) {
        StrikesManager.totalChests = totalChests;
    }
    public static void resetCurrentChests() {
        StrikesManager.currentChests = 0;
    }

    public static int getCurrentChests() {
        return currentChests;
    }

    public static int getTotalChests() {
        return totalChests;
    }

    public static void onActionbar(Text content, CallbackInfo ci) {
        if (content == null) return;
        Matcher m = CHESTS_PATTERN.matcher(ChatUtils.stripFormatting(content.getString()));
        if (m.matches()) {
            currentChests = Integer.parseInt(m.group(1));
        }
    }
}