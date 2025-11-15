package dev.mme.features.tooltip;

import dev.mme.listener.KeyListener;
import dev.mme.util.ChatUtils;
import dev.mme.util.FS;
import dev.mme.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class TooltipScreenshotter  {
    public static @Nullable ItemStack toScreenshot;
    private static final TooltipPositioner NO_POSITIONER = (int screenWidth, int screenHeight, int x, int y, int width, int height) -> new Vector2i(4, 4);
    public static void screenshotToClipboard(ItemStack stack) {
        final var mc = MinecraftClient.getInstance();
        List<TooltipComponent> components = stack.getTooltip(mc.player, mc.options.advancedItemTooltips ? TooltipContext.ADVANCED : TooltipContext.BASIC).stream().map(Text::asOrderedText).map(TooltipComponent::of).toList();
        stack.getTooltipData().ifPresent((datax) -> components.add(1, TooltipComponent.of(datax)));
        int width = 0;
        for (TooltipComponent c : components) {
            int w = c.getWidth(mc.textRenderer);
            if (w > width) {
                width = w;
            }
        }
        width += 8;
        int height = 16;
        if (components.size() > 1) {
            height += 2 + (components.size() - 1) * 10;
        }

        float scaleh = (float) mc.getWindow().getScaledHeight() / height;
        float scalew = (float) mc.getWindow().getScaledWidth() / width;

        MinecraftClient.getInstance().getFramebuffer().endWrite();

        DrawContext drawContext = new DrawContext(MinecraftClient.getInstance(), VertexConsumerProvider.immediate(new BufferBuilder(256)));

        Framebuffer framebuffer = new WindowFramebuffer(width * 2, height * 2);
        framebuffer.setClearColor(1, 1, 1, 0);
        framebuffer.initFbo(width * 2, height * 2, false);
        framebuffer.beginWrite(false);

        drawContext.getMatrices().push();
        drawContext.getMatrices().scale(scalew, scaleh, 1);
        drawContext.drawTooltip(mc.textRenderer, components, 0, 0, NO_POSITIONER);
        drawContext.getMatrices().pop();
        drawContext.draw();
        framebuffer.endWrite();
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);

        try (NativeImage image = fromFramebuffer(framebuffer)) {
            BufferedImage buffered = javax.imageio.ImageIO.read(new ByteArrayInputStream(image.getBytes()));
            if (MinecraftClient.IS_SYSTEM_MAC) {
                FS.mkParents("cache/tooltip.png");
                File cachePath = FS.locate("cache/tooltip.png");
                image.writeTo(cachePath);
                String[] cmd = {"osascript", "-e", "tell app \"Finder\" to set the clipboard to ( POSIX file \""+cachePath.getAbsolutePath()+"\" )"};
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (Exception ex) {
                    ChatUtils.logError(ex, "Caught exception while copying to clipboard");
                }
            } else {
                final var transferable = new ImageTransferable(buffered);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, transferable);
            }
        } catch (IOException ex) {
            ChatUtils.logError(ex, "Caught exception whilst saving screenshot");
        }
    }

    private static NativeImage fromFramebuffer(Framebuffer framebuffer) {
        final NativeImage img = new NativeImage(framebuffer.textureWidth, framebuffer.textureHeight, false);

        // This call internally binds the buffer's color attachment texture
        framebuffer.beginRead();

        // This method gets the pixels from the currently bound texture
        img.loadFromTextureImage(0, false);
        img.mirrorVertically();

        framebuffer.delete();

        return img;
    }

    static {
        KeyListener.EVENT.register((InputUtil.Key key, int action, CallbackInfo ci) -> {
            if (action == GLFW.GLFW_PRESS && (key.getCode() == 67 && Screen.hasControlDown() && !Screen.hasAltDown())) {
                final var item = Utils.getHoveredItem();
                if (item.isPresent()) {
                    toScreenshot = item.get();
                    ci.cancel();
                }
            }
        });
    }
}