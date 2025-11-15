package dev.mme;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.mme.MMEConfig;
import dev.mme.core.TickScheduler;
import dev.mme.features.tooltip.czcharms.CZCharmDB;
import dev.mme.util.ChatUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;

import java.io.IOException;
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
        dispatcher.register(builder);
    }

}