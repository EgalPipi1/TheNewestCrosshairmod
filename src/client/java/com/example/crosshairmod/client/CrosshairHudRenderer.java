package com.example.crosshairmod.client;

import com.example.crosshairmod.config.CrosshairConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Renders {@link CrosshairConfig#count} crosshairs around the screen center,
 * using the same vanilla crosshair sprite the game itself uses, so the
 * extras look identical to your normal crosshair.
 *
 * Positions come from {@link CrosshairMath}, the same grid the
 * {@link CrosshairInteractionHandler} uses for aiming, so what you see is
 * exactly where you can attack/mine. Index 0 (dead center) is skipped here
 * since that's vanilla's own crosshair, already drawn by the game.
 */
public final class CrosshairHudRenderer {

    private static final Identifier CROSSHAIR_TEXTURE = Identifier.ofVanilla("hud/crosshair");
    private static final int VANILLA_SIZE = 16;

    private CrosshairHudRenderer() {
    }

    public static void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.options.hudHidden) return;
        if (client.player == null) return;

        int count = CrosshairConfig.count;
        if (count <= 1) return; // nothing extra to draw beyond vanilla's own crosshair

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        List<CrosshairMath.Offset> offsets =
                CrosshairMath.computeOffsets(count, CrosshairConfig.range, CrosshairConfig.density);

        int size = CrosshairConfig.size > 0 ? CrosshairConfig.size : VANILLA_SIZE;
        int half = size / 2;

        float a = ((CrosshairConfig.color >>> 24) & 0xFF) / 255f;
        float r = ((CrosshairConfig.color >>> 16) & 0xFF) / 255f;
        float g = ((CrosshairConfig.color >>> 8) & 0xFF) / 255f;
        float b = (CrosshairConfig.color & 0xFF) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(r, g, b, a);

        // Skip index 0: that's the vanilla center crosshair, already drawn by the game.
        for (int i = 1; i < offsets.size(); i++) {
            CrosshairMath.Offset offset = offsets.get(i);
            int x = centerX + (int) Math.round(offset.dx()) - half;
            int y = centerY + (int) Math.round(offset.dy()) - half;
            context.drawGuiTexture(CROSSHAIR_TEXTURE, x, y, size, size);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
