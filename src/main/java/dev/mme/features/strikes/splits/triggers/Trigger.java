package dev.mme.features.strikes.splits.triggers;

import dev.mme.util.ChatUtils;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public interface Trigger extends Comparable<Trigger> {
    String name();

    enum MatchMode {
        STARTS_WITH,
        CONTAINS,
        SERVER_MESSAGE_CONTAINS
    }

    boolean triggers(@NotNull Text content, Object... args);

    static boolean contentMatches(@NotNull Text content, Trigger trigger) {
        String str = ChatUtils.stripFormatting(content.getString());
        return switch (trigger.matchMode()) {
            case STARTS_WITH -> str.startsWith(trigger.name());
            case CONTAINS -> str.contains(trigger.name());
            case SERVER_MESSAGE_CONTAINS -> str.contains(trigger.name()) && !str.contains("»");
        };
    }

    default MatchMode matchMode() {
        return MatchMode.SERVER_MESSAGE_CONTAINS;
    }

    default int compareTo(@NotNull Trigger o) {
        return name().compareTo(o.name());
    }
}