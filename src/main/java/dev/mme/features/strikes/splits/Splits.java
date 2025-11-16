package dev.mme.features.strikes.splits;

import com.google.common.reflect.TypeToken;
import dev.mme.MMEClient;
import dev.mme.core.TextBuilder;
import dev.mme.features.strikes.splits.triggers.BossBarPercentTrigger;
import dev.mme.features.strikes.splits.triggers.RegexTrigger;
import dev.mme.features.strikes.splits.triggers.StringTrigger;
import dev.mme.features.strikes.splits.triggers.Trigger;
import dev.mme.listener.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Splits implements ClientTickEvents.EndTick, ChatListener, ClientBossBarListener, ActionbarListener, SubtitleListener, TitleListener, ClientLoginConnectionEvents.Disconnect {
    private static final List<SplitTimer> builtinSplits = new ArrayList<>();
    private static final List<SplitTimer> customSplits = new ArrayList<>();
    public static final Pattern TRANSFER_PATTERN = Pattern.compile("(?i)Transferring you to (.+)");
    private RegistryKey<DimensionType> dimension;

    @Override
    public void onLoginDisconnect(ClientLoginNetworkHandler handler, MinecraftClient client) {
        shutdown();
    }

    public static void shutdown() {
        for (SplitTimer split : builtinSplits) {
            if (split.active) split.done();
        }
        for (SplitTimer split : customSplits) {
            if (split.active) split.done();
        }
        MMEClient.SCOREBOARD.setContentSupplier(null);
    }

    public static class CustomSplitsConfig extends dev.mme.core.Config<Map<String, CustomSplit>> {
        public static final CustomSplitsConfig INSTANCE = new CustomSplitsConfig();
        private CustomSplitsConfig() {
            super("customSplits.json", new HashMap<>(Map.of("Tutorial Location", new CustomSplit(new ArrayList<>(List.of(
                    SplitsTrigger.of("[TUTORIAL] This message triggers phase 1", TriggerType.CHAT, new TextBuilder("Phase 1").withFormat(Formatting.GOLD).build()),
                    SplitsTrigger.of("[TUTORIAL] This title triggers phase 2", TriggerType.TITLE, new TextBuilder("Phase 2").withFormat(Formatting.GOLD).build()),
                    SplitsTrigger.of("[TUTORIAL] This subtitle triggers phase 3", TriggerType.SUBTITLE, new TextBuilder("Phase 3").withFormat(Formatting.GOLD).build()),
                    SplitsTrigger.of("[TUTORIAL] This actionbar triggers phase 4", TriggerType.ACTIONBAR, new TextBuilder("Phase 4").withFormat(Formatting.GOLD).build()),
                    SplitsTrigger.of(new RegexTrigger("\\[TUTORIAL] This bossbar \\(anything goes here (.+)\\) will trigger phase 5"), TriggerType.BOSSBAR_ADD, new TextBuilder("Phase 5").withFormat(Formatting.GOLD).build()),
                    SplitsTrigger.of(new BossBarPercentTrigger("[TUTORIAL] This bossBar upon reaching the percent will trigger phase 6", 50), TriggerType.BOSSBAR_PERCENT, new TextBuilder("Phase 6").withFormat(Formatting.GOLD).build())
            )),
                    SplitsTrigger.of("[TUTORIAL] This trigger triggers the split", TriggerType.CHAT, null),
                    SplitsTrigger.of("[TUTORIAL] This trigger ends the split", TriggerType.CHAT, null)))), new TypeToken<Map<String, CustomSplit>>(){}.getType());
        }
        @Override
        protected void loadJson() throws IOException {
            super.loadJson();
            customSplits.clear();
            config.forEach((name, split) -> customSplits.add(new SplitTimer(name, split)));
        }

        public void importSplit(String name, CustomSplit split) throws IOException {
            config.put(name, split);
            customSplits.add(new SplitTimer(name, split));
            saveJson();
        }
    }

    public Splits() {
        ClientTickEvents.END_CLIENT_TICK.register(this);
        ChatListener.EVENT.register(this);
        ClientBossBarListener.EVENT.register(this);
        ActionbarListener.EVENT.register(this);
        SubtitleListener.EVENT.register(this);
        TitleListener.EVENT.register(this);
    }

    public static class Config {
        public boolean enable = true;
        public boolean showTooltipInstead = true;
        public boolean showChestCount = true;
        public boolean checkmarkWhenDone = true;
    }

    public static Config config() {
        return MMEClient.CONFIG.get().splits;
    }

    public enum TriggerType {
        CHAT,
        TITLE,
        SUBTITLE,
        ACTIONBAR,
        BOSSBAR_ADD,
        BOSSBAR_REMOVE,
        BOSSBAR_PERCENT
    }

    public record SplitsTrigger(Trigger trigger, TriggerType triggerType, @Nullable Text displayName) {
        public static SplitsTrigger of(Object trigger, TriggerType triggerType, @Nullable Object displayName) {
                if (!(trigger instanceof Trigger)) trigger = new StringTrigger(trigger.toString());
                if (displayName == null) return new SplitsTrigger((Trigger) trigger, triggerType, null);
                if (displayName instanceof Text text) return new SplitsTrigger((Trigger) trigger, triggerType, text);
                if (displayName instanceof TextBuilder textBuilder)
                    return new SplitsTrigger((Trigger) trigger, triggerType, textBuilder.build());
                return new SplitsTrigger((Trigger) trigger, triggerType, Text.literal(displayName.toString()));
            }

            @Override
            public Text displayName() {
                return this.displayName != null ? this.displayName : Text.literal("");
            }

            public boolean triggers(Text content, TriggerType type, Object... args) {
                if (type != triggerType() || content == null) return false;
                return trigger().triggers(content, args);
            }
        }

    public record CustomSplit(@Nullable List<SplitsTrigger> triggers, SplitsTrigger initTrigger,
                              SplitsTrigger endTrigger) {
        @Override
        public List<SplitsTrigger> triggers() {
                return triggers != null ? triggers : List.of();
            }
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (!config().enable) return;
        if (client.world != null) {
            if (dimension != null && !dimension.equals(client.world.getDimensionKey())) {
                shutdown();
            }
            dimension = client.world.getDimensionKey();
        }
        for (SplitTimer split : builtinSplits) {
            split.tick();
        }
        for (SplitTimer split : customSplits) {
            split.tick();
        }
    }

    @Override
    public void onChat(Text message, CallbackInfo ci) {
        if (!config().enable) return;
        if (TRANSFER_PATTERN.matcher(message.getString()).matches()) {
            shutdown();
            ClientWorld world = MinecraftClient.getInstance().world;
            if (world != null) {
                dimension = world.getDimensionKey();
            }
        }
        for (SplitTimer split : builtinSplits) {
            split.onTrigger(message, TriggerType.CHAT);
        }
        for (SplitTimer split : customSplits) {
            split.onTrigger(message, TriggerType.CHAT);
        }
    }

    @Override
    public void onActionbar(Text message, CallbackInfo ci) {
        if (!config().enable) return;
        for (SplitTimer split : builtinSplits) {
            split.onTrigger(message, TriggerType.ACTIONBAR);
        }
        for (SplitTimer split : customSplits) {
            split.onTrigger(message, TriggerType.ACTIONBAR);
        }
    }

    @Override
    public void onSubtitle(Text message, CallbackInfo ci) {
        if (!config().enable) return;
        for (SplitTimer split : builtinSplits) {
            split.onTrigger(message, TriggerType.SUBTITLE);
        }
        for (SplitTimer split : customSplits) {
            split.onTrigger(message, TriggerType.SUBTITLE);
        }
    }

    @Override
    public void onTitle(Text message, CallbackInfo ci) {
        if (!config().enable) return;
        for (SplitTimer split : builtinSplits) {
            split.onTrigger(message, TriggerType.TITLE);
        }
        for (SplitTimer split : customSplits) {
            split.onTrigger(message, TriggerType.TITLE);
        }
    }

    @Override
    public void onBossBar(Type type, UUID uuid) {
        if (!config().enable) return;
        ClientBossBar bar = getBar(uuid);
        Text name = getName(uuid);
        final TriggerType triggerType = switch (type) {
            case ADD -> TriggerType.BOSSBAR_ADD;
            case REMOVE -> TriggerType.BOSSBAR_REMOVE;
            case UPDATE_PERCENT -> TriggerType.BOSSBAR_PERCENT;
            default -> null;
        };
        if (triggerType != null) {
            for (SplitTimer split : builtinSplits) {
                split.onTrigger(name, triggerType, bar);
            }
            for (SplitTimer split : customSplits) {
                split.onTrigger(name, triggerType, bar);
            }
        }
    }
    static {
        builtinSplits.add(ZenithSplit.INSTANCE);
        builtinSplits.add(BMSplit.INSTANCE);
        builtinSplits.add(PortalSplit.INSTANCE);
        builtinSplits.add(SKTSplit.INSTANCE);
        CustomSplitsConfig.INSTANCE.getClass();
    }
}