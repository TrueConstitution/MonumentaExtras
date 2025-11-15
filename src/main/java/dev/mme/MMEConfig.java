package dev.mme;

import dev.mme.core.Scoreboard;
import dev.mme.features.solvers.SpellEstimator;
import dev.mme.features.solvers.content.skr.SKRSolvers;
import dev.mme.features.strikes.splits.Splits;
import dev.mme.features.tooltip.czcharms.CZCharmAnalysis;
import dev.mme.features.tooltip.czcharms.CZCharmDB;
import dev.mme.util.FS;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

@Config(name="mmev2")
public class MMEConfig implements ConfigData {
    @ConfigEntry.Category("solvers")
    @ConfigEntry.Gui.TransitiveObject
    public SolverConfig solvers = new SolverConfig();

    @ConfigEntry.Category("czcharmanalysis")
    @ConfigEntry.Gui.TransitiveObject
    public CZCharmAnalysis.Config czcharmanalysis = new CZCharmAnalysis.Config();

    @ConfigEntry.Category("czcharmdb")
    @ConfigEntry.Gui.TransitiveObject
    public CZCharmDB.Config czcharmdb = new CZCharmDB.Config();

    @ConfigEntry.Category("scoreboard")
    @ConfigEntry.Gui.TransitiveObject
    public Scoreboard.Config scoreboard = new Scoreboard.Config();

    @ConfigEntry.Category("splits")
    @ConfigEntry.Gui.TransitiveObject
    public Splits.Config splits = new Splits.Config();

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.TransitiveObject
    public MiscConfig misc = new MiscConfig();

    public static class SolverConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public SpellEstimator.Config spellEstimator = new SpellEstimator.Config();
        @ConfigEntry.Gui.CollapsibleObject
        public SKRSolvers.Config skr = new SKRSolvers.Config();
    }

    public static class MiscConfig {
        public boolean lerpColor = true;
        public boolean useLocalShardsOverride = false;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> ConfigHolder<MMEConfig> register() {
        ConfigHolder<MMEConfig> holder = AutoConfig.register(
                MMEConfig.class, (config, clazz) -> new GsonConfigSerializer<>(config, clazz, FS.GSON)
        );
        AutoConfig.getGuiRegistry(MMEConfig.class).registerTypeProvider((i18n, field, config, defaults, guiProvider) -> Collections.singletonList(
                ConfigEntryBuilder.create().startStrList(
                                Text.translatable(i18n),
                        ((Map<K, V>) getUnsafely(field, config)).entrySet().stream().map(e -> String.format("%s:%s", e.getKey(), e.getValue())).toList()
                        )
                        .setDefaultValue(() -> defaults == null ? null : ((Map<K, V>)getUnsafely(field, defaults)).entrySet().stream().map(e -> String.format("%s:%s", e.getKey(), e.getValue())).toList())
                        .setSaveConsumer(newValue -> {
                            MapType annotation = field.getAnnotation(MapType.class);
                            Map<K, V> result = new HashMap<>();
                            for (String entry : newValue) {
                                String[] parts = entry.split(":", 2); // Split on first colon
                                if (parts.length < 2) continue;

                                K key = (K) FS.GSON.fromJson("\"" + parts[0] + "\"", annotation.key());
                                V value = (V) FS.GSON.fromJson("\"" + parts[1] + "\"", annotation.value());

                                result.put(key, value);
                            }
                            setUnsafely(field, config, result);
                        })
                        .build()
        ), Map.class);
        return holder;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface MapType {
        Class<?> key();
        Class<?> value();
    }
}
