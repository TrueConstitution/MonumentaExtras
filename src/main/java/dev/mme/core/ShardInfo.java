package dev.mme.core;

import com.google.common.reflect.TypeToken;
import dev.mme.MMEClient;
import dev.mme.features.tooltip.czcharms.CZCharmDB;
import dev.mme.mixin.access.PlayerListHudAccessor;
import dev.mme.util.FS;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShardInfo extends Config<Map<String, String>> {
    public static final ShardInfo INSTANCE = new ShardInfo();
    public static final Pattern TAB_PATTERN = Pattern.compile("Connected to proxy: <(.+)> shard: <(.+)>");
    private ShardInfo() {
        super("shards.json", null, new TypeToken<Map<String, String>>(){}.getType());
    }

    @Override
    protected void init() {
        if (MMEClient.CONFIG.get().misc.useLocalShardsOverride) {
            super.init();
            return;
        }
        try {
            config = MMEAPI.fetchGHContent("shards.json", new TypeToken<>() {});
            this.saveJson();
        } catch (IOException ignored) {}
    }

    public String getShard() {
        Text header = ((PlayerListHudAccessor) MinecraftClient.getInstance().inGameHud.getPlayerListHud()).mme$getHeader();
        if (header == null) return "unknown";
        Matcher m = TAB_PATTERN.matcher(header.getString());
        if (m.matches()) return m.group(2);
        return "unknown";
    }

    public String getProxy() {
        Text header = ((PlayerListHudAccessor) MinecraftClient.getInstance().inGameHud.getPlayerListHud()).mme$getHeader();
        if (header == null) return "unknown";
        Matcher m = TAB_PATTERN.matcher(header.getString());
        if (m.matches()) return m.group(1);
        return "unknown";
    }

    public String getLocation() {
        String rawShard = getShard().split("-")[0];
        return config.getOrDefault(rawShard, rawShard);
    }
}
