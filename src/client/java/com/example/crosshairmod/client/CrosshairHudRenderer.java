package com.example.crosshairmod.client;

import com.example.crosshairmod.config.CrosshairConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/**
 * Renders {@link CrosshairConfig#count} crosshairs around the screen center.
 *
 * Positions come from {@link CrosshairMath}'s ordered grid, the same one
 * {@link CrosshairInteractionHandler} uses for aiming, so what you see is
 * exactly where you can attack/mine. Index 0 (dead center) is skipped here
 * since that's vanilla's own crosshair, already drawn by the game.
 *
 * Each crosshair is drawn as a thin white cross with a dark outline, similar
 * in proportions to vanilla's crosshair (a small cross, not a thick blob),
 * so it stays visible against both light and dark backgrounds.
 */
public final class CrosshairHudRenderer {

    private static final int OUTLINE_COLOR = 0xC0000000; // semi-transparent black

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

        // Skip index 0: that's the vanilla center crosshair, already drawn by the game.
        for (int i = 1; i < offsets.size(); i++) {
            CrosshairMath.Offset offset = offsets.get(i);
            int x = centerX + (int) Math.round(offset.dx());
            int y = centerY + (int) Math.round(offset.dy());
            drawCrosshair(context, x, y);
        }
    }

    private static void drawCrosshair(DrawContext context, int x, int y) {
        int half = Math.max(1, CrosshairConfig.size / 2);
        int thickHalf = Math.max(1, CrosshairConfig.thickness / 2);
        int color = CrosshairConfig.color;

        // Dark outline first (slightly larger), so the cross stays visible
        // against light backgrounds, similar to how vanilla's crosshair reads
        // clearly against snow, sand, or bright skies.
        context.fill(x - half - 1, y - thickHalf - 1, x + half + 1, y + thickHalf + 1, OUTLINE_COLOR);
        context.fill(x - thickHalf - 1, y - half - 1, x + thickHalf + 1, y + half + 1, OUTLINE_COLOR);

        // Main white cross on top.
        context.fill(x - half, y - thickHalf, x + half, y + thickHalf, color);
        context.fill(x - thickHalf, y - half, x + thickHalf, y + half, color);
    }
}
