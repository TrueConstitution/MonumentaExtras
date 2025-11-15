package dev.mme.features.tooltip.czcharms;

import java.util.Random;

public enum CharmType {
    SINGLE_ABILITY("Ability", 1),
    TREE_LOCKED("Treelocked", 1.35),
    WILDCARD("Wildcard", 1.8);
    public final String name;
    public final double budgetMultiplier;

    CharmType(String s, double budgetMultiplier) {
        this.name = s;
        this.budgetMultiplier = budgetMultiplier;
    }

    public static CharmType getType(int charmType) {
        if (charmType < 4) return SINGLE_ABILITY;
        if (charmType < 9) return TREE_LOCKED;
        else return WILDCARD;
    }

    public static CharmType findType(int charmPower, long seed) {
        Random r = new Random(seed);
        int mCharmType = r.nextInt(10);
        if (charmPower > 3) mCharmType = Math.max(mCharmType + 2, 4);
        return getType(mCharmType);
    }
}