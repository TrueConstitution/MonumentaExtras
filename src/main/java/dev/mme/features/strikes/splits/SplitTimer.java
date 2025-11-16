package dev.mme.features.strikes.splits;

import dev.mme.MMEClient;
import dev.mme.core.TextBuilder;
import dev.mme.util.ChatUtils;
import dev.mme.util.ColorUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public class SplitTimer {
    public final Splits.CustomSplit split;
    public final String name;
    protected int phase = -1;
    private final int[] timesInTicks;
    protected int ticksElapsed = 0;
    protected boolean active;
    public SplitTimer(String name, Splits.CustomSplit split) {
        this.split = split;
        this.name = name;
        this.timesInTicks = new int[split.triggers().size()];
    }

    public List<Text> getContent() {
        List<Text> content = new ArrayList<>();
        content.add(new TextBuilder(" ⏣ ").withFormat(Formatting.GRAY).append(name).withFormat(Formatting.RED).build());
        content.add(new TextBuilder("").build());
        addSplitTimerText(content);
        return content;
    }

    protected void addSplitTimerText(List<Text> content) {
        content.add(new TextBuilder(" Time Elapsed: ").append(toFormattedTimeSB(ticksElapsed)).withFormat(Formatting.GREEN).build());
        for (int i = 0; i < timesInTicks.length; i++) {
            content.add(new TextBuilder(" ").append(this.split.triggers().get(i).displayName()).append(": ")
                    .append(toFormattedTimeNormal(timesInTicks[i])).withFormat(Formatting.GREEN).build());
        }
    }

    public static String toFormattedTimeSB(int ticks) {
        int mins = ticks / 1200;
        int secs = ticks / 20 - (mins * 60);
        return String.format(Locale.US, "%s%02ds", mins > 0 ? String.format(Locale.US, "%02dm ", mins) : "", secs);
    }

    public static String toFormattedTimeNormal(double ticks) {
        int mins = (int) ticks / 1200;
        double secs = ticks / 20 - (mins * 60);
        return String.format(Locale.ROOT, "%s%.2fs", mins > 0 ? String.format(Locale.ROOT, "%02dm ", mins) : "", secs);
    }

    public static Text format(Text phase, String time) {
        return new TextBuilder(phase).append(Text.translatable("text.mmev2.splits.time_message", time)).build();
    }

    public void tick() {
        if (!active) return;
        if (phase > -1 && phase < timesInTicks.length) {
            ticksElapsed++;
            timesInTicks[phase]++;
        }
    }

    protected void start() {
        Arrays.fill(timesInTicks, 0);
        ticksElapsed = 0;
        MMEClient.SCOREBOARD.setContentSupplier(this::getContent);
        active = true;
    }

    protected void done() {
        this.phase = -1;
        active = false;
        ChatUtils.logInfo(format(this.split.triggers().get(timesInTicks.length - 1).displayName(), toFormattedTimeNormal(timesInTicks[timesInTicks.length - 1])));
        TextBuilder builder = new TextBuilder(String.format(Locale.US, "%s cleared in %s", name, toFormattedTimeNormal(ticksElapsed)));
        List<Text> formatted = IntStream.range(0, timesInTicks.length)
                .mapToObj(i -> format(this.split.triggers().get(i).displayName(), toFormattedTimeNormal(timesInTicks[i])))
                .toList();
        if (Splits.config().showTooltipInstead) {
            formatted.stream().map(TextBuilder::new).reduce((a, b) -> a.append("\n").append(b)).map(TextBuilder::build).ifPresent(builder::withShowTextHover);
            ChatUtils.logInfo(builder.build());
        }
        else {
            ChatUtils.logInfo(builder.build());
            formatted.forEach(ChatUtils::logInfo);
        }
    }

    public void onTrigger(Text content, Splits.TriggerType type, Object... args) {
        if (split.initTrigger().triggers(content, type, args)) {
            this.start();
        }
        if (this.active && split.endTrigger().triggers(content, type, args)) {
            this.done();
            return;
        }
        if (this.phase + 1 < this.split.triggers().size() && split.triggers().get(this.phase + 1).triggers(content, type, args)) {
            this.phase++;
        }
    }

    protected Text getObjectiveProgress(int curr, int total) {
        if (Splits.config().checkmarkWhenDone && curr >= total) {
            return new TextBuilder("✓").withFormat(Formatting.GREEN).build();
        }
        return new TextBuilder(String.format(Locale.ROOT, "%d/%d", curr, total))
                .withColor(ColorUtils.getPercentageColor((float) 100 * curr/total)).build();
    }
}
