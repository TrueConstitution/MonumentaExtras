package dev.mme;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.mme.core.Config;
import dev.mme.core.MMEAPI;
import dev.mme.core.TickScheduler;
import dev.mme.features.strikes.splits.Splits;
import dev.mme.features.cz.CZCharmDB;
import dev.mme.util.ChatUtils;
import dev.mme.util.Reflections;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;


public class MMECommand implements ClientCommandRegistrationCallback {
    public MMECommand() {
        ClientCommandRegistrationCallback.EVENT.register(this);
    }
    private enum ArgumentTypes {
        quotedStringArg(toArg(StringArgumentType::string)),
        wordArg(toArg(StringArgumentType::word)),
        greedyStringArg(toArg(StringArgumentType::greedyString)),
        booleanArg(toArg(BoolArgumentType::bool)),
        intArg(toArg((Supplier<ArgumentType<?>>) IntegerArgumentType::integer)),
        doubleArg(toArg((Supplier<ArgumentType<?>>) DoubleArgumentType::doubleArg));

        public final ArgumentType<?> arg;

        ArgumentTypes(ArgumentType<?> argument) {
            this.arg = argument;
        }

        private static ArgumentType<?> toArg(Supplier<ArgumentType<?>> type) {
            return type.get();
        }
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal("mme").executes(ctx -> {
            TickScheduler.INSTANCE.schedule(0, client -> client.execute(() -> client.setScreen(AutoConfig.getConfigScreen(MMEConfig.class, null).get())));
            return 1;
        });

        LiteralArgumentBuilder<FabricClientCommandSource> czcharms = ClientCommandManager.literal("czcharms");
        czcharms.then(ClientCommandManager.literal("exportdb").executes(ctx -> {
            try {
                CZCharmDB.INSTANCE.export();
                return ChatUtils.logInfo("Successfully exported charmdb.json -> charmdb.txt for mtce!");
            } catch (IOException e) {
                return ChatUtils.logError(e, "Caught exception while exporting db");
            }
        }));
        builder.then(czcharms);

        LiteralArgumentBuilder<FabricClientCommandSource> importCMD = ClientCommandManager.literal("import");
        importCMD.then(ClientCommandManager.literal("splits").then(ClientCommandManager.argument("name", ArgumentTypes.greedyStringArg.arg).suggests((ctx, bder) -> CommandSource.suggestMatching(MMEAPI.getJsonFilesInPath("imports/splits/"), bder)).executes(ctx -> {
            String toImport = ctx.getArgument("name", String.class);
            CompletableFuture.runAsync(() -> {
                try {
                    Splits.CustomSplit remoteSplit = MMEAPI.fetchGHContent("imports/splits/" + toImport + ".json", Splits.CustomSplit.class);
                    Splits.CustomSplitsConfig.INSTANCE.importSplit(toImport, remoteSplit);
                } catch (IOException ex) {
                    ChatUtils.logError(ex, "Caught error while importing split " + toImport);
                }
            });
            return 1;
        })));
        builder.then(importCMD);

        LiteralArgumentBuilder<FabricClientCommandSource> reloadCMD = ClientCommandManager.literal("reload");
        reloadCMD.executes(ctx -> {
            MMEClient.CONFIG.load();
            Reflections.getInstances(Reflections.DEFAULT.getSubTypesOf(Config.class))
                    .forEach(c -> Reflections.invokeMethod(c, "loadJson"));
            return ChatUtils.logInfo("Reloaded all config!");
        });
        builder.then(reloadCMD);
        
        dispatcher.register(builder);
    }

}