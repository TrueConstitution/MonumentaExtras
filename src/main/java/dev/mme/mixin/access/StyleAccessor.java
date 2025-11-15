package dev.mme.mixin.access;

import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Style.class)
public interface StyleAccessor {
    @Nullable
    @Accessor("color")
    TextColor mme$getColor();
    @Nullable
    @Accessor("bold")
    Boolean mme$getBold();
    @Nullable
    @Accessor("italic")
    Boolean mme$getItalic();
    @Nullable
    @Accessor("underlined")
    Boolean mme$getUnderlined();
    @Nullable
    @Accessor("strikethrough")
    Boolean mme$getStrikethrough();
    @Nullable
    @Accessor("obfuscated")
    Boolean mme$getObfuscated();
    @Nullable
    @Accessor("font")
    Identifier mme$getFont();
}