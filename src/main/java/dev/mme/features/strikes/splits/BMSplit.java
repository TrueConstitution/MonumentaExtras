package dev.mme.features.strikes.splits;

import dev.mme.core.TextBuilder;
import dev.mme.features.strikes.StrikesManager;
import dev.mme.util.ChatUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class BMSplit extends SplitTimer {
    public static final BMSplit INSTANCE = new BMSplit();
    private boolean minibossKilled = false;
    private BMSplit() {
        super("The Black Mist", new Splits.CustomSplit(List.of(
                Splits.SplitsTrigger.of("[Varcosa] Ye best not lose them lanterns, matey. They be your only ticket out of here.", Splits.TriggerType.CHAT, "Clear"),
                Splits.SplitsTrigger.of("Your class has been temporarily disabled.", Splits.TriggerType.CHAT, "Cutscene"),
                Splits.SplitsTrigger.of("Your class has been reenabled.", Splits.TriggerType.CHAT, "Waves"),
                Splits.SplitsTrigger.of("Fine! Ye make me do this meself!", Splits.TriggerType.CHAT, "Varcosa"),
                Splits.SplitsTrigger.of("Yarr... why be this hurtin'? I shan't go!", Splits.TriggerType.CHAT, "Lingering Will")
        ),
                Splits.SplitsTrigger.of("Black Mist", Splits.TriggerType.TITLE, null),
                Splits.SplitsTrigger.of("I feel it... partin'... the beyond calls... and I answer...", Splits.TriggerType.CHAT, null)));
    }

    @Override
    public List<Text> getContent() {
        List<Text> content = new ArrayList<>();
        content.add(new TextBuilder(" ⏣ ").withFormat(Formatting.GRAY).append("The Black Mist").withFormat(Formatting.RED).build());
        content.add(new TextBuilder().build());
        content.add(new TextBuilder("Miniboss").append(": ").append(minibossKilled ? "§a✓" : "§c✗").build());
        if (Splits.config().showChestCount) {
            content.add(new TextBuilder(" Chests: ").withFormat(Formatting.WHITE)
                    .append(getObjectiveProgress(StrikesManager.getCurrentChests(), StrikesManager.getTotalChests()))
                    .build());
        }
        content.add(new TextBuilder().build());

        addSplitTimerText(content);
        return content;
    }

    @Override
    protected void start() {
        StrikesManager.overrideTotalChests(42);
        StrikesManager.resetCurrentChests();
        super.start();
    }

    @Override
    public void onTrigger(Text content, Splits.TriggerType type, Object... args) {
        if (content != null && content.getString().startsWith("[Varcosa] So ye be takin' me admiral's loot fer yeself? Ye'll pay dearly fer that!")) {
            minibossKilled = true;
            ChatUtils.logInfo(SplitTimer.format(Text.literal("Miniboss"), toFormattedTimeNormal(ticksElapsed)));
        }
        super.onTrigger(content, type, args);
    }
}