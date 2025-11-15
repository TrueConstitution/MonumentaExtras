package dev.mme;

import dev.mme.features.solvers.SpellEstimator;
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

import java.util.*;
import java.util.stream.Collectors;

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

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.TransitiveObject
    public MiscConfig misc = new MiscConfig();

    public static class SolverConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public SpellEstimator.Config spellEstimator = new SpellEstimator.Config();
    }

    public static class MiscConfig {
        public boolean lerpColor = true;
    }

    public static ConfigHolder<MMEConfig> register() {
        ConfigHolder<MMEConfig> holder = AutoConfig.register(
                MMEConfig.class, (config, clazz) -> new GsonConfigSerializer<>(config, clazz, FS.GSON)
        );
        AutoConfig.getGuiRegistry(MMEConfig.class).registerTypeProvider((i18n, field, config, defaults, guiProvider) -> Collections.singletonList(
                ConfigEntryBuilder.create().startStrList(
                                Text.translatable(i18n),
                                getUnsafely(field, config, new HashMap<>()).entrySet().stream().map(e -> String.format("%s:%s", e.getKey(), e.getValue())).toList()
                        )
                        .setDefaultValue(() -> defaults == null ? null : ((StrIntMap)getUnsafely(field, defaults)).entrySet().stream().map(e -> String.format("%s:%s", e.getKey(), e.getValue())).toList())
                        .setSaveConsumer(newValue -> setUnsafely(field, config, new StrIntMap(newValue.stream().map(s -> s.split(":")).collect(Collectors.toMap(p -> p[0], p -> Integer.valueOf(p[1]))))))
                        .build()), StrIntMap.class);
        return holder;
    }

    public static class StrIntMap extends HashMap<String, Integer> {
        public StrIntMap() {
            super();
        }
        public StrIntMap(Map<String, Integer> map) {
            super(map);
        }
    }
}
