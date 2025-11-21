package dev.mme.features.cz;

import dev.mme.core.TextBuilder;
import net.minecraft.text.Text;

public enum DepthsTree {
    Dawnbringer(0xf0b326),
    Earthbound(0x6b3d2d),
    Flamecaller(0xf04e21),
    Frostborn(0xa3cbe1),
    Steelsage(0x929292),
    Shadowdancer(0x7948af),
    Windwalker(0xc0dea9);
    public final int color;
    public final Text displayName;
    public final Text displayNameShort;

    DepthsTree(int color) {
        this.color = color;
        this.displayName = new TextBuilder(name()).withColor(color).build();
        this.displayNameShort = new TextBuilder(name().substring(0, 2)).withColor(color).build();
    }

    public static DepthsTree getTree(DepthsAbilityInfo member) {
        return member.tree;
    }
}