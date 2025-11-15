package dev.mme.features.strikes.splits.triggers;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record StringTrigger(String string, MatchMode matchMode) implements Trigger {
    public StringTrigger(String string) {
        this(string, MatchMode.SERVER_MESSAGE_CONTAINS);
    }

    @Override
    public String name() {
        return string;
    }

    @Override
    public boolean triggers(@NotNull Text content, Object... args) {
        return Trigger.contentMatches(content, this);
    }
}