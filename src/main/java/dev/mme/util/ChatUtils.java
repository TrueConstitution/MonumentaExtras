package dev.mme.util;

import dev.mme.core.TextBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public abstract class ChatUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Pattern STRIP_FORMATTING_PATTERN = Pattern.compile("§[0-9A-FK-OR]", Pattern.CASE_INSENSITIVE);
    public static int logInfo(Object object) {
        if (object instanceof TextBuilder builder) {
            return logInfo(builder.build());
        } else if (object instanceof Text text) {
            return logInfo(text);
        }
        return logInfo(object.toString());
    }

    public static int logInfo(String text) {
        return logInfo(Text.literal(text));
    }

    public static int logInfo(Text text) {
        log(new TextBuilder().append("[MME] ")
                .withFormat(Formatting.DARK_AQUA)
                .append(text).build());
        return 1;
    }

    public static int logError(Throwable exception, @Nullable String message) {
        return logInfo(new TextBuilder(Text.translatable("text.mmev2.uncaught_exception_message", message)).withClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Utils.logError(exception, message)));
    }

    public static void log(Text text) {
        mc.inGameHud.getChatHud().addMessage(text);
    }

    public static String stripFormatting(String string) {
        return STRIP_FORMATTING_PATTERN.matcher(string).replaceAll("");
    }
}
