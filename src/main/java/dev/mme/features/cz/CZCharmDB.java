package dev.mme.features.cz;

import com.google.common.reflect.TypeToken;
import dev.mme.MMEClient;
import dev.mme.core.TickScheduler;
import dev.mme.listener.InteractBlockListener;
import dev.mme.util.ChatUtils;
import dev.mme.util.FS;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CZCharmDB implements InteractBlockListener {
    public static final CZCharmDB INSTANCE = new CZCharmDB();
    public CZCharmDB() {
        InteractBlockListener.EVENT.register(this);
    }

    public static class Config {
        @ConfigEntry.Gui.PrefixText
        public boolean enable = true;
        public boolean checkInsideShulkers = true;
        public Mode mode = Mode.Blacklist;
        public List<String> containerTitles = List.of("Ender Chest", "Shulker Box", "Crafting", "Firmament", "Worldshaper's Loom", "Potion Injector", "Iridium Injector", "P.I.D.S.", "Ender Chest Expansion", "Equipment Case");
    }

    private static BlockPos lastInteractBlock = null;

    public static Config config() {
        return MMEClient.CONFIG.get().cz.charmdb;
    }

    @Override
    public void onUse(BlockHitResult blockHitResult, BlockState blockState) {
        lastInteractBlock = blockHitResult.getBlockPos();
    }

    record DataObject(CZCharm charm, String shard, Vector3d lastInteractBlock, Vector3d playerPos, String containerTitle) {
    }

    public enum Mode {
        Whitelist,
        Blacklist
    }

    public static class DB {
        private static final String PATH = "cz/charmdb.json";
        private static final Map<String, DataObject> db = new HashMap<>();
        public static void saveJson() throws IOException {
            FS.writeJsonFile(new TreeMap<>(db), PATH);
        }
        public static void init() throws IOException {
            FS.readJsonFile(new TypeToken<Map<String, DataObject>>(){}.getType(), PATH);
        }
    }

    private static boolean isCharmEffectMenu(ScreenHandler handler) {
        return handler.getSlot(9).getStack().getName().getString().strip().equals("Charm Effect Summary");
    }

    public void parseCharms(HandledScreen<?> screen) {
        Config config = config();
        if (!config.enable) return;
        TickScheduler.INSTANCE.schedule(1, client -> {
        ScreenHandler handler = screen.getScreenHandler();
        String title = screen.getTitle().getString();
        if (config.mode == Mode.Blacklist) {
            if (isCharmEffectMenu(handler) || config.containerTitles.contains(title)) return;
        } else if (!config.containerTitles.contains(title)) {
            return;
        }
            if (client.world == null || client.player == null) return;
            boolean hasChanged = false;
            for (int i = 0; i < handler.slots.size()-36; i++) {
                ItemStack stack = handler.slots.get(i).getStack();
                if (config.checkInsideShulkers && stack.getItem().toString().contains("shulker_box")) {
                    NbtList storedItems = (NbtList) stack.getOrCreateNbt().getCompound("BlockEntityTag").get("Items");
                    if (storedItems != null) {
                        for (int j = 0; j < storedItems.size(); j++) {
                            NbtCompound item = storedItems.getCompound(j).getCompound("tag");
                            if (CZCharm.isZenithCharm(item)) {
                                hasChanged = true;
                                CZCharm charm = CZCharm.parseNBT(item);
                                BlockPos playerPos = client.player.getBlockPos();
                                Vector3d lastInteractPos = lastInteractBlock == null ? null : new Vector3d(lastInteractBlock.getX(), lastInteractBlock.getY(), lastInteractBlock.getZ());
                                DB.db.put(Long.toHexString(charm.uuid()), new DataObject(charm, client.world.getRegistryKey().getValue().getPath(), lastInteractPos, new Vector3d(playerPos.getX(), playerPos.getY(), playerPos.getZ()), title + ":" + i + ":" + stack.getName().getString() + ":" + j));
                            }
                        }
                    }
                }
                if (CZCharm.isZenithCharm(stack.getOrCreateNbt())) {
                    hasChanged = true;
                    CZCharm charm = CZCharm.parseNBT(stack.getOrCreateNbt());
                    BlockPos playerPos = client.player.getBlockPos();
                    Vector3d lastInteractPos = lastInteractBlock == null ? null : new Vector3d(lastInteractBlock.getX(), lastInteractBlock.getY(), lastInteractBlock.getZ());
                    DB.db.put(Long.toHexString(charm.uuid()), new DataObject(charm, client.world.getRegistryKey().getValue().getPath(), lastInteractPos, new Vector3d(playerPos.getX(), playerPos.getY(), playerPos.getZ()), title + ":" + i));
                }
            }
            if (hasChanged) {
                try {
                    DB.saveJson();
                } catch (IOException ignored) {
                }
            }
        }).exceptionally(e -> {
            ChatUtils.logError(e, "Caught error while parsing charms to db");
            return null;
        });
    }

    public void export() throws IOException {
        StringBuilder result = new StringBuilder();
        for (DataObject data : DB.db.values()) {
            String res = String.format(
                    "%s;%s;%s;%s;%s",
                    data.charm.rarity().mRarity-1,
                    data.charm.name() + ":" + data.charm.uuid(),
                    data.charm.charmPower(),
                    data.charm.effects().stream().map(x -> x.effect().effectName.toLowerCase().replace(" ", "_").replace("'", "")).collect(Collectors.joining(":")),
                    data.charm.effects().stream().map(x -> String.format(Locale.ROOT, "%.2f", x.value())).collect(Collectors.joining(":"))
            );
            if (data.charm.upgrade() != data.charm) {
                res = String.format("%s;%s", res, data.charm.upgrade().effects().stream().map(x -> String.format(Locale.ROOT, "%.2f", x.value())).collect(Collectors.joining(":")));
            }
            result.append(res);
            result.append("\n");
        }
        FS.write("cz/charmdb.txt", result.toString());
    }
}