package dev.mme.core;

import com.google.gson.JsonParseException;
import dev.mme.MMEClient;
import dev.mme.features.tooltip.czcharms.DepthsAbilityInfo;
import dev.mme.util.FS;
import dev.mme.util.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class Config<T> {
    protected final String CONFIG_PATH;
    protected T config;
    protected Type type;
    public final boolean customClass;

    protected Config(String configPath, T defaultConfig, Type type) {
        this.CONFIG_PATH = configPath;
        this.config = defaultConfig;
        this.customClass = false;
        this.type = type;
        this.init();
    }

    protected void loadJson() throws IOException, JsonParseException {
        this.config = FS.readJsonFile(type, CONFIG_PATH);
    }

    protected void saveJson() throws IOException {
        if (this.config instanceof Map<?, ?> mp) FS.writeJsonFile(new TreeMap<>(mp), CONFIG_PATH);
        if (this.config instanceof Set<?> S) FS.writeJsonFile(new TreeSet<>(S), CONFIG_PATH);
        else FS.writeJsonFile(this.config, CONFIG_PATH);
    }

    protected void saveDefaultConfig() throws IOException {
        saveJson();
    }

    protected void init() {
        try {
            loadJson();
        } catch (FileNotFoundException e) {try{saveDefaultConfig();}catch(IOException ignored){}}
        catch (JsonParseException ex) {
            try{
                FS.locate(CONFIG_PATH).renameTo(FS.locate(CONFIG_PATH + ".old"));
                saveDefaultConfig();
                Utils.logError(ex, "Neutralizing old config due to bad config");
            }catch(IOException ignored){}
        }catch(IOException ignored){}
    }
}