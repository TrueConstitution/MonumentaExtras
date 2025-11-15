package dev.mme.features.strikes.splits.triggers;

import dev.mme.util.ChatUtils;
import dev.mme.util.Utils;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record RegexTrigger(String regex) implements Trigger {
    public RegexTrigger(String regex) {
        this.regex = regex;
        Utils.verifyPattern(regex);
    }

    @Override
    public String name() {
        return regex;
    }

    @Override
    public boolean triggers(@NotNull Text content, Object... args) {
        return ChatUtils.stripFormatting(content.getString()).matches(regex);
    }
}