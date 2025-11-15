package dev.mme.features.tooltip.czcharms;

import org.jetbrains.annotations.Nullable;

public enum CZCharmRarity {
    N_LEGENDARY("Negative Legendary", 5, 15, true),
    N_EPIC("Negative Epic", 4, 13, true),
    N_RARE("Negative Rare", 3, 10, true),
    N_UNCOMMON("Negative Uncommon", 2, 7, true),
    N_COMMON("Negative Common", 1, 4, true),
    COMMON("Common", 1, -5, false, 0x9f929c),
    UNCOMMON("Uncommon", 2, -8, false, 0x70bc6d),
    RARE("Rare", 3, -11, false, 0x705eca),
    EPIC("Epic", 4, -14, false, 0xcd5eca),
    LEGENDARY("Legendary", 5, -16, false, 0xe49b20);

    public final String mAction;
    public final int mRarity;
    public final int mBudget;
    public final boolean mIsNegative;
    public final int rgb;

    CZCharmRarity(String name, int rarity, int budget, boolean isNegative) {
        this(name, rarity, budget, isNegative, 0xff5555);
    }

    CZCharmRarity(String name, int rarity, int budget, boolean isNegative, int rgb) {
        mAction = name;
        mRarity = rarity;
        mBudget = budget;
        mIsNegative = isNegative;
        this.rgb = rgb;
    }

    public static @Nullable CZCharmRarity getEffect(String actionName) {
        for (CZCharmRarity ce : CZCharmRarity.values()) {
            if (ce.mAction.equals(actionName)) {
                return ce;
            }
        }
        return null;
    }

    public CZCharmRarity upgrade() {
        if (this == LEGENDARY) throw new UnsupportedOperationException("already at max rarity");
        return values()[ordinal()+1];
    }

    public int upgradeCost() {
        return this.upgrade().mBudget - mBudget;
    }
}