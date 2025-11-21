package dev.mme.features.strikes.splits;

import dev.mme.core.TextBuilder;
import dev.mme.features.strikes.StrikesManager;
import dev.mme.util.ChatUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortalSplit extends SplitTimer {
    public static final PortalSplit INSTANCE = new PortalSplit();
    private static final Pattern NODES_PATTERN = Pattern.compile("Power node readings: Complete. Status: (\\d+)/3 nodes powered\\.");
    private static final Pattern SOULS_PATTERN = Pattern.compile("\\+1 S\\.O\\.U\\.L\\. Collected : (\\d+)/350");
    private static final int totalNodes = 3;
    private static final int totalSouls = 350;
    private int nodes = 0;
    private int souls = 0;

    private PortalSplit() {
        super("P.O.R.T.A.L.", new Splits.CustomSplit(List.of(
                Splits.SplitsTrigger.of("P.O.R.T.A.L.", Splits.TriggerType.TITLE, "Clear"),
                Splits.SplitsTrigger.of("Iota", Splits.TriggerType.BOSSBAR_ADD, "Iota")
        ),
                Splits.SplitsTrigger.of("P.O.R.T.A.L.", Splits.TriggerType.TITLE, null),
                Splits.SplitsTrigger.of("Iota", Splits.TriggerType.BOSSBAR_REMOVE, null)));
    }

    @Override
    protected void start() {
        StrikesManager.overrideTotalChests(41);
        StrikesManager.resetCurrentChests();
        this.nodes = 0;
        this.souls = 0;
        super.start();
    }

    @Override
    public List<Text> getContent() {
        List<Text> content = new ArrayList<>();
        content.add(new TextBuilder(" ⏣ ").withFormat(Formatting.GRAY).append("The Celestial Zenith").withFormat(Formatting.RED).build());
        content.add(new TextBuilder().build());
        if (Splits.config().showChestCount) {
            content.add(new TextBuilder(" Nodes: ").withFormat(Formatting.WHITE).append(getObjectiveProgress(nodes, totalNodes)).build());
            content.add(new TextBuilder(" Chests: ").withFormat(Formatting.WHITE).append(getObjectiveProgress(StrikesManager.getCurrentChests(), StrikesManager.getTotalChests())).build());
            content.add(new TextBuilder(" Souls: ").withFormat(Formatting.WHITE).append(getObjectiveProgress(souls, totalSouls)).build());
        }
        addSplitTimerText(content);
        return content;
    }

    @Override
    public void onTrigger(Text content, Splits.TriggerType type, Object... args) {
        if (content != null) {
            Matcher m = NODES_PATTERN.matcher(content.getString());
            if (m.matches()) {
                nodes = Integer.parseInt(m.group(1));
                if (nodes == totalNodes) {
                    ChatUtils.logInfo(SplitTimer.format(Text.literal("Nodes"), toFormattedTimeNormal(ticksElapsed)));
                }
            } else {
                Matcher m2 = SOULS_PATTERN.matcher(content.getString());
                if (m2.matches()) {
                    souls = Integer.parseInt(m2.group(1));
                    if (souls == totalSouls) {
                        ChatUtils.logInfo(SplitTimer.format(Text.literal("Souls"), toFormattedTimeNormal(ticksElapsed)));
                    }
                }
            }
        }
        super.onTrigger(content, type, args);
    }
}