package dev.mme.features.solvers.content.skr;

import com.google.common.reflect.TypeToken;
import dev.mme.MMEClient;
import dev.mme.core.Config;
import dev.mme.core.MMEAPI;
import dev.mme.core.TextBuilder;
import dev.mme.core.TickScheduler;
import dev.mme.listener.ChatListener;
import dev.mme.listener.ItemUseListener;
import dev.mme.listener.JoinedPacketListener;
import dev.mme.util.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SKRScrollSolver implements JoinedPacketListener, ChatListener, ItemUseListener {
    public SKRScrollSolver() {
        JoinedPacketListener.EVENT.register(this);
        ChatListener.EVENT.register(this);
        ItemUseListener.EVENT.register(this);
    }
    private static Item currentScroll = null;
    private static Vector3d startPos = null;
    private static Vector3d endPos = null;
    private static Vector3f startColor = null;
    private static Vector3f endColor = null;
    private static class RiddleData extends Config<Map<String, Vector3d>> {
        public static final RiddleData INSTANCE = new RiddleData();
        private RiddleData() {
            super("skr/riddledata.json", new HashMap<>(), new TypeToken<Map<String, Vector3d>>(){}.getType());
        }

        @Override
        protected void init() {
            if (SKRSolvers.config().useLocalRiddleDataOverride) {
                super.init();
                return;
            }
            try {
                Map<String, Vector3d> newConfig = MMEAPI.fetchGHContent("skr/riddledata.json", new TypeToken<>() {});
                if (newConfig == null) {
                    super.init();
                    return;
                }
                config = newConfig;
                this.saveJson();
            } catch (IOException ignored) {}
        }

        public Vector3d solveRiddle(String riddle) {
            return config.get(riddle);
        }
    }
    @Override
    public void onJoinedPacket(Packet<?> packet, CallbackInfo ci) {
        if (!SKRSolvers.config().enable) return;
        if (currentScroll == null) return;
        CompletableFuture.runAsync(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && !client.player.getItemCooldownManager().isCoolingDown(currentScroll)) {
                currentScroll = null;
                Vector3d directionVec = new Vector3d(endPos).sub(startPos).normalize();
                Vector3d predictedPos = new Vector3d(directionVec.mul(estimateDistanceRemaining(startColor, endColor))).add(endPos);
                String formatted = String.format(Locale.ROOT, "(%.2f, %.2f, %.2f)",
                        predictedPos.x,
                        predictedPos.y,
                        predictedPos.z);
                ChatUtils.logInfo("Predicted location: " + formatted);
                String command = String.format(
                        "/xaero_waypoint_add:SKR Scroll:S:%d:%d:%d:3:false:0:Internal-dim%%%s",
                        (int) predictedPos.x, (int) predictedPos.y, (int) predictedPos.z, Objects.requireNonNull(MinecraftClient.getInstance().world).getRegistryKey().getValue().toString().replace(":", "$")
                );
                ChatUtils.logInfo(new TextBuilder("Click to add waypoint").withClickEvent(ClickEvent.Action.RUN_COMMAND, command).build());
                startPos = null;
                endPos = null;
                startColor = null;
                endColor = null;
                return;
            }
            if (packet instanceof ParticleS2CPacket particle) {
                ParticleEffect effect = particle.getParameters();
                if (effect instanceof DustColorTransitionParticleEffect dustEffect) {
                    endPos = new Vector3d(particle.getX(), particle.getY(), particle.getZ());
                    endColor = dustEffect.getToColor().mul(255);
                    if (startColor == null) {
                        startColor = endColor;
                    }
                }
            }
        });
    }

    @Override
    public void onChat(Text message, CallbackInfo ci) {
        if (!SKRSolvers.config().enable) return;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !player.getMainHandStack().getName().getString().contains("Remnant Scroll")) return;
        Vector3d loc = RiddleData.INSTANCE.solveRiddle(message.getString().strip());
        if (loc != null) {
            ChatUtils.logInfo("The SKR Scroll Location is at " + loc);
            String command = String.format(
                    "/xaero_waypoint_add:SKR Scroll:S:%d:%d:%d:3:false:0:Internal-dim%%%s",
                    (int) loc.x, (int) loc.y, (int) loc.z, Objects.requireNonNull(MinecraftClient.getInstance().world).getRegistryKey().getValue().toString().replace(":", "$")
            );
            ChatUtils.logInfo(new TextBuilder("Click to add waypoint").withClickEvent(ClickEvent.Action.RUN_COMMAND, command).build());
        }
    }

    @Override
    public void onUse(World world, PlayerEntity user, Hand hand) {
        if (!SKRSolvers.config().enable) return;
        if (startPos == null && user.getMainHandStack().getName().getString().contains("Remnant Scroll")) {
            startPos = new Vector3d(user.getX(), user.getY(), user.getZ());
            TickScheduler.INSTANCE.schedule(2, client -> currentScroll = user.getMainHandStack().getItem());
        }
    }

    private enum DistanceBand {
        VERY_FAR(new Vector3f(255, 0, 0), 600),
        FAR(new Vector3f(255, 165, 0), 400),
        MEDIUM(new Vector3f(255, 255, 0), 200),
        CLOSE(new Vector3f(0, 128, 0), 50);

        public final Vector3f color;
        public final double distanceMidpoint;

        DistanceBand(Vector3f color, double distanceMidpoint) {
            this.color = color;
            this.distanceMidpoint = distanceMidpoint;
        }

        public static DistanceBand fromColor(Vector3f color) {
            DistanceBand closest = CLOSE;
            double best = Double.MAX_VALUE;

            for (DistanceBand band : values()) {
                double d = band.color.distance(color);
                if (d < best) {
                    best = d;
                    closest = band;
                }
            }
            return closest;
        }
    }

    private static double estimateDistanceRemaining(Vector3f startColor, Vector3f endColor) {
        DistanceBand startBand = DistanceBand.fromColor(startColor);
        DistanceBand endBand = DistanceBand.fromColor(endColor);

        if (startBand == endBand) {
            return startBand.distanceMidpoint;
        }

        if (startBand.ordinal() < endBand.ordinal()) {
            return endBand.distanceMidpoint;
        }

        return (startBand.distanceMidpoint + endBand.distanceMidpoint) / 2.0;
    }
}