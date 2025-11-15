package dev.mme.features.strikes.splits;

import dev.mme.core.TextBuilder;
import dev.mme.features.strikes.splits.triggers.StringTrigger;
import dev.mme.features.strikes.splits.triggers.Trigger;
import dev.mme.util.ChatUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ZenithSplit extends SplitTimer {
    private boolean rewardFound = true;
    public static final ZenithSplit INSTANCE = new ZenithSplit();
    private ZenithSplit() {
        super("The Celestial Zenith", new Splits.CustomSplit(List.of(
                Splits.SplitsTrigger.of(new StringTrigger("[Zenith Party] Spawned new ", Trigger.MatchMode.STARTS_WITH), Splits.TriggerType.CHAT, "F1 Clear"),
                Splits.SplitsTrigger.of("Callicarpa", Splits.TriggerType.TITLE, "Callicarpa"),
                Splits.SplitsTrigger.of("[Callicarpa] Oh yes, at last, the voice recedes... with final breath... I am now freed...", Splits.TriggerType.CHAT, "F2 Clear"),
                Splits.SplitsTrigger.of("The Broodmother", Splits.TriggerType.TITLE, "The Broodmother"),
                Splits.SplitsTrigger.of("[Zenith Party] You received a celestial gift for clearing the floor! Check your trinket to see the upgrade.", Splits.TriggerType.CHAT, "F3 Clear"),
                Splits.SplitsTrigger.of("The Vesperidys", Splits.TriggerType.TITLE, "The Vesperidys")
        ),
                Splits.SplitsTrigger.of(new StringTrigger("Transferring you to zenith", Trigger.MatchMode.STARTS_WITH), Splits.TriggerType.CHAT, null),
                Splits.SplitsTrigger.of("[The Vesperidys] You will be devoured by the vessels still above.", Splits.TriggerType.CHAT, null)));
    }

    @Override
    public List<Text> getContent() {
        List<Text> content = new ArrayList<>();
        content.add(new TextBuilder(" ⏣ ").withFormat(Formatting.GRAY).append("The Celestial Zenith").withFormat(Formatting.RED).build());
        content.add(new TextBuilder().build());
        content.add(new TextBuilder(rewardFound ? " Reward Found: §a✓" : " Reward Found: §c✗").build());
        addSplitTimerText(content);
        return content;
    }

    @Override
    protected void start() {
        this.rewardFound = true;
        super.start();
    }

    @Override
    public void onTrigger(Text content, Splits.TriggerType type, Object... args) {
        if (content != null && ChatUtils.stripFormatting(content.getString()).startsWith("[Zenith Party] This room's ")) {
            this.rewardFound = true;
        }
        if (content != null && ChatUtils.stripFormatting(content.getString()).startsWith("[Zenith Party] Spawned new ") &&
                (content.getString().contains("Ability") || content.getString().contains("Upgrade"))) {
            this.rewardFound = false;
        }
        if (content != null && ChatUtils.stripFormatting(content.getString()).startsWith("[Zenith Party] Sending you to loot room ")
                && phase != -1) {
            super.done();
            return;
        }
        super.onTrigger(content, type, args);
    }
}