package dev.mme.features.strikes.splits;

import dev.mme.core.TextBuilder;
import dev.mme.features.strikes.StrikesManager;
import dev.mme.features.strikes.splits.triggers.StringTrigger;
import dev.mme.features.strikes.splits.triggers.Trigger;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class SKTSplit extends SplitTimer {
    public static final SKTSplit INSTANCE = new SKTSplit();
    private static final int totalMiniboss = 4;
    private int miniboss = 0;

    private SKTSplit() {
        super("Silver Knight's Tomb", new Splits.CustomSplit(List.of(
                Splits.SplitsTrigger.of(new StringTrigger("Transferring you to skt", Trigger.MatchMode.STARTS_WITH), Splits.TriggerType.CHAT, "Clear"),
                Splits.SplitsTrigger.of(new StringTrigger("[Silver Construct] UNAUTHORIZED ENTITY DETECTED: IMPLEMENTING REMOVAL PROCEDURE", Trigger.MatchMode.STARTS_WITH), Splits.TriggerType.CHAT, "Construct"),
                Splits.SplitsTrigger.of(new StringTrigger("[Silver Construct] PRIME DIRECTIVE FAILED: TOMB HAS BEEN BREACHED. ENABLING FORGE DEFENCES.", Trigger.MatchMode.STARTS_WITH), Splits.TriggerType.CHAT, "Forge"),
                Splits.SplitsTrigger.of(new StringTrigger("[The Crimson King] It is done. Together we have crafted a new wool. It is blank now. Soon we shall grant it a color. It is time to enter the crypt itself, where the Silver Knight lies. Where the Malefactor himself waits for us. I need you to know I will not be of much help in this coming fight, however.", Trigger.MatchMode.STARTS_WITH), Splits.TriggerType.CHAT, "Cutscene"),
                Splits.SplitsTrigger.of(new StringTrigger("[The Malefactor] then i wish you luck trying. you can never stop me. it is far too late.", Trigger.MatchMode.STARTS_WITH), Splits.TriggerType.CHAT, "Teal")
        ),
                Splits.SplitsTrigger.of(new StringTrigger("Transferring you to skt", Trigger.MatchMode.STARTS_WITH), Splits.TriggerType.CHAT, null),
                Splits.SplitsTrigger.of(new StringTrigger("no, this cannot be! time... betrays me? why do the hands of time not turn? i... will... be... forever!", Trigger.MatchMode.STARTS_WITH), Splits.TriggerType.CHAT, null)));
    }

    @Override
    public List<Text> getContent() {
        List<Text> content = new ArrayList<>();
        content.add(new TextBuilder(" ⏣ ").withFormat(Formatting.GRAY).append("The Celestial Zenith").withFormat(Formatting.RED).build());
        content.add(new TextBuilder().build());
        if (Splits.config().showChestCount) {
            content.add(new TextBuilder(" Minibosses: ").withFormat(Formatting.WHITE).append(getObjectiveProgress(miniboss, totalMiniboss)).build());
            content.add(new TextBuilder(" Chests: ").withFormat(Formatting.WHITE).append(getObjectiveProgress(StrikesManager.getCurrentChests(), StrikesManager.getTotalChests())).build());
        }
        addSplitTimerText(content);
        return content;
    }

    @Override
    protected void start() {
        this.miniboss = 0;
        StrikesManager.overrideTotalChests(97);
        StrikesManager.resetCurrentChests();
        super.start();
    }

    @Override
    public void onTrigger(Text content, Splits.TriggerType type, Object... args) {
        if (content != null && type == Splits.TriggerType.CHAT) {
            String s = content.getString();
            switch (s) {
                case "The time rift fades away as you land the finishing blow, and you find yourself back where you were.":
                    miniboss++;
                    break;
                case "A loud rumbling emanates from the Cathedral, echoing through the city as the final time rift collapses.":
                    miniboss = totalMiniboss;
                    break;
                default:
                    break;
            }
        }
        super.onTrigger(content, type, args);
    }
}