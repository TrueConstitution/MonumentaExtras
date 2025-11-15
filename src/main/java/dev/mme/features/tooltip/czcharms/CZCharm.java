package dev.mme.features.tooltip.czcharms;

import dev.mme.core.TextBuilder;
import dev.mme.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public record CZCharm(long uuid, String name, int charmPower, CZCharmRarity rarity, List<CZCharmEffect> effects, int budgetUsed, CharmType type, boolean upgraded) {
    private static final int[] CHARM_BUDGET_PER_POWER = {2, 4, 7, 11, 16};

    public static boolean isZenithCharm(NbtCompound nbt) {
        return nbt.getCompound("Monumenta").getString("Tier").equals("zenithcharm");
    }

    private static int getBudget(CZCharmRarity charmRarity, int charmPower, CharmType charmType) {
        return (int) Math.floor((charmRarity.mRarity + 1)
                * CHARM_BUDGET_PER_POWER[charmPower - 1] * charmType.budgetMultiplier) - charmRarity.mBudget;
    }

    public int calcBudget() {
        return getBudget(rarity, charmPower, type);
    }

    public static CZCharm parseStack(NbtCompound nbt) {
        NbtCompound monumenta = nbt.getCompound("Monumenta");
        NbtCompound playerModified = monumenta.getCompound("PlayerModified");
        long uuid = playerModified.getLong("DEPTHS_CHARM_UUID");
        int charmPower = monumenta.getInt("CharmPower");
        CZCharmRarity charmRarity = CZCharmRarity.values()[5+playerModified.getInt("DEPTHS_CHARM_RARITY") - 1];
        int effectCount = playerModified.getKeys()
                .stream()
                .filter(x -> x.startsWith("DEPTHS_CHARM_EFFECT"))
                .map(x -> x.substring("DEPTHS_CHARM_EFFECT".length()))
                .map(Integer::parseInt)
                .reduce(Integer::max).orElseThrow(() -> new IllegalStateException("this cz charm does not have effects"));
        List<CZCharmEffect> effectList = new ArrayList<>();

        for (int i = 1; i <= effectCount; i++) {
            double roll = playerModified.getDouble("DEPTHS_CHARM_ROLLS" + i);
            DepthsAbilityInfo effect = DepthsAbilityInfo.get(playerModified.getString("DEPTHS_CHARM_EFFECT" + i));
            CZCharmRarity effectRarity;
            if (playerModified.contains("DEPTHS_CHARM_ACTIONS" + (i-1))) {
                effectRarity = CZCharmRarity.getEffect(playerModified.getString("DEPTHS_CHARM_ACTIONS" + (i - 1)));
            } else {
                effectRarity = charmRarity;
            }
            effectList.add(new CZCharmEffect(roll, effect, effectRarity));
        }
        int budgetUsed = playerModified.getInt("DEPTHS_CHARM_BUDGET") - charmRarity.mBudget;
        CharmType type = CharmType.getType(playerModified.getInt("DEPTHS_CHARM_TYPE_ROLL"));
        return new CZCharm(uuid, nbt.getCompound("plain").getCompound("display").getString("Name"), charmPower, charmRarity, effectList, budgetUsed, type, playerModified.getBoolean("CELESTIAL_GEM_USED"));
    }

    public CZCharm upgrade() {
        if (upgraded || rarity == CZCharmRarity.LEGENDARY) return this;
        CZCharmRarity newRarity = rarity.upgrade();
        int newBudget = getBudget(newRarity, charmPower, type);
        List<CZCharmEffect> newEffects = new ArrayList<>(effects);
        newEffects.set(0, effects.get(0).upgrade());
        int remainingBudget = newBudget - budgetUsed + (newRarity.mBudget - rarity.mBudget);
        for (boolean hasUpgrade = true; hasUpgrade;) {
            hasUpgrade = false;
            for (int i = 1; i < effects.size(); i++) {
                CZCharmEffect effect = newEffects.get(i);
                if (effect.canUpgrade(newRarity, remainingBudget)) {
                    remainingBudget += effect.rarity().upgradeCost();
                    newEffects.set(i, newEffects.get(i).upgrade());
                    hasUpgrade = true;
                }
            }
        }
        return new CZCharm(uuid, name, charmPower, newRarity, newEffects, newBudget-remainingBudget, type, true);
    }

    public List<DepthsTree> trees() {
        return effects.stream().map(e -> e.effect().tree).distinct().toList();
    }

    public double roll() {
        return effects.stream().mapToDouble(CZCharmEffect::roll).average().orElse(0);
    }

    public Text displayName() {
        double rollPercent = roll()*100;
        return new TextBuilder(name).withFormat(Formatting.BOLD).withColor(rarity.rgb).append(String.format(" [%.1f%%]", rollPercent)).resetMarkdowns().withColor(ColorUtils.getPercentageColor((float) rollPercent)).build();
    }

    public List<Text> lore() {
        List<Text> result = new ArrayList<>();
        result.add(new TextBuilder("Architect's Ring : ").withFormat(Formatting.DARK_GRAY).append("Zenith Charm").withColor(0xFF9CF0).build());
        TextBuilder powerLine = new TextBuilder("Charm Power : ").withFormat(Formatting.DARK_GRAY)
                .append("★".repeat(this.charmPower)).withColor(0xFFFA75)
                .append(" - ").withFormat(Formatting.DARK_GRAY)
                .append(this.rarity.mAction).withColor(this.rarity.rgb);
        if (this.upgraded) {
            powerLine.append(" (❃)").withColor(0x9374FF);
        }
        result.add(powerLine.build());
        if (CZCharmAnalysis.config().showBudget) {
            result.add(new TextBuilder("Budget : ").withFormat(Formatting.DARK_GRAY)
                    .append(String.format(Locale.ROOT, "%d/%d", budgetUsed, calcBudget()))
                    .withFormat(Formatting.GRAY).build());
        }
        result.add(new TextBuilder(this.type.name).withFormat(Formatting.GRAY)
                .append(" - ").withFormat(Formatting.DARK_GRAY)
                .append(switch (type) {
                    case SINGLE_ABILITY -> this.effects.get(0).effect().getAbilityDisplayName();
                    case TREE_LOCKED -> this.effects.get(0).effect().tree.displayName;
                    case WILDCARD -> this.trees().stream()
                            .map(t -> new TextBuilder(t.displayNameShort))
                            .reduce((a, b) -> a.append(",")
                                    .withFormat(Formatting.DARK_GRAY).append(b)).map(TextBuilder::build).orElseThrow();
                }).build());
        result.add(Text.empty());
        Text[] headers;
        if (type == CharmType.SINGLE_ABILITY) {
            headers = new Text[]{Text.of("Stat"), Text.of("Effect"), Text.of("Roll")};
        } else {
            headers = new Text[]{Text.of("Stat"), Text.of("Ability"), Text.of("Effect"), Text.of("Roll")};
        }
        Text[][] table = new Text[effects.size()+1][headers.length];
        table[0] = headers;
        DecimalFormat df = new DecimalFormat("+0.##;-0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
        CZCharm upgradedCharm = this.upgrade();
        List<CZCharmEffect> upgradedEffects = upgradedCharm.effects();
        for (int i = 1; i <= effects.size(); i++) {
            CZCharmEffect effect = effects.get(i-1);
            table[i][0] = new TextBuilder(df.format(effect.value()) + (effect.effect().isPercent ? "%" : "")).withColor(effect.rarity().rgb).build();
            if (Screen.hasShiftDown() && upgradedCharm != this) {
                CZCharmEffect upgradedEffect = upgradedEffects.get(i-1);
                if (upgradedEffect.rarity().ordinal() > effect.rarity().ordinal()) {
                    table[i][0] = new TextBuilder(table[i][0])
                            .append(" -> ").withFormat(Formatting.GRAY)
                            .append(df.format(upgradedEffect.value()) + (upgradedEffect.effect().isPercent ? "%" : "")).withColor(upgradedEffect.rarity().rgb).build();
                }
            }
            if (headers.length == 4) {
                table[i][1] = effect.effect().getAbilityDisplayName();
            }
            table[i][headers.length-2] = new TextBuilder(effect.effect().modifierName()).withColor(effect.rarity().rgb).build();
            table[i][headers.length-1] = new TextBuilder(String.format(Locale.ROOT, "%.2f%%", effect.roll()*100)).withColor(ColorUtils.getPercentageColor((float) (100*effect.roll()))).build();
            if (CZCharmAnalysis.config().displayMode == CZCharmAnalysis.DisplayMode.Compact) {
                table[i][headers.length-1] = new TextBuilder("[").withFormat(Formatting.GRAY).append(table[i][headers.length-1]).append("]").withFormat(Formatting.GRAY).build();
            }
        }
        if (CZCharmAnalysis.config().displayMode == CZCharmAnalysis.DisplayMode.Tabular) {
            result.addAll(Arrays.asList(tabulate(table)));
        } else {
            result.addAll(Arrays.asList(table).subList(1, table.length).stream().map(tArr -> {
                TextBuilder builder = new TextBuilder();
                for (Text t : tArr) {
                    builder.append(t).append(" ");
                }
                return builder.build();
            }).toList());
        }
        if (CZCharmAnalysis.config().showUUID) {
            result.add(new TextBuilder("UUID: ").withFormat(Formatting.DARK_GRAY).append(new UUID(uuid, 0).toString().replaceAll("(-0+)+$", "")).withFormat(Formatting.GRAY).build());
        }
        return result;
    }

    private static Text[] tabulate(Text[][] table) {
        var client = MinecraftClient.getInstance();
        var TR = client != null ? client.textRenderer : null;
        if (TR == null || table.length == 0) return new Text[0];

        int rows = table.length;
        int cols = table[0].length;
        Text[] lines = new Text[rows];

        for (int i = 0; i < rows; i++) {
            lines[i] = Text.empty();
        }

        for (int col = 0; col < cols; col++) {
            int maxWidth = 0;
            int[] widths = new int[rows];

            // Measure widths per cell for this column
            for (int row = 0; row < rows; row++) {
                if (table[row][col] == null) table[row][col] = Text.empty();
                widths[row] = TR.getWidth(table[row][col]);
                if (widths[row] > maxWidth) maxWidth = widths[row];
            }

            // Align and append each cell
            for (int row = 0; row < rows; row++) {
                Text aligned = new TextBuilder(table[row][col])
                        .align(TextBuilder.Alignment.LEFT, maxWidth)
                        .build();

                TextBuilder builder = new TextBuilder(lines[row])
                        .append(aligned);

                if (col + 1 < cols) builder.append(Text.literal(" "));

                lines[row] = builder.build();
            }
        }

        return lines;
    }
}