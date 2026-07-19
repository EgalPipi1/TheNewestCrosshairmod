package com.example.crosshairmod.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared grid math used by both the HUD renderer and the interaction
 * handler, so the crosshairs you see are exactly the ones you can act on.
 *
 * Crosshairs are arranged in a deterministic, ordered grid (row by row,
 * left to right) centered on the screen -- not a random scatter.
 */
public final class CrosshairMath {

    private CrosshairMath() {
    }

    /** A single crosshair's offset, in pixels, from the screen center. */
    public record Offset(double dx, double dy) {
    }

    /**
     * Computes pixel offsets for `count` crosshairs arranged in a grid:
     *   - The grid is sized to fit `count` items as close to square as possible.
     *   - `range` controls how far the outermost row/column sits from center.
     *   - `density` controls spacing: higher = crosshairs packed tighter
     *     together, lower = spread further apart.
     * Index 0 is always the exact screen center (the vanilla crosshair's spot).
     */
    public static List<Offset> computeOffsets(int count, float range, float density) {
        List<Offset> offsets = new ArrayList<>(Math.max(0, count));
        if (count <= 0) return offsets;

        if (count == 1) {
            offsets.add(new Offset(0, 0));
            return offsets;
        }

        float safeDensity = Math.max(0.05f, density);

        int columns = (int) Math.ceil(Math.sqrt(count));
        int rows = (int) Math.ceil((double) count / columns);

        double maxSpan = Math.max(columns - 1, rows - 1);
        if (maxSpan < 1) maxSpan = 1;

        // Base spacing so the grid's outer edge sits at `range` pixels from
        // center, then density tightens (>1) or loosens (<1) that spacing.
        double spacing = (range / maxSpan) / safeDensity;

        int placed = 0;
        for (int row = 0; row < rows && placed < count; row++) {
            for (int col = 0; col < columns && placed < count; col++) {
                double dx = (col - (columns - 1) / 2.0) * spacing;
                double dy = (row - (rows - 1) / 2.0) * spacing;
                offsets.add(new Offset(dx, dy));
                placed++;
            }
        }

        // Ensure index 0 is exactly the center point (swap it to the front
        // if the grid math didn't already place it there), since the
        // interaction handler treats index 0 as "vanilla's own crosshair".
        int centerIndex = closestToCenterIndex(offsets);
        if (centerIndex > 0) {
            Offset centerOffset = offsets.remove(centerIndex);
            offsets.add(0, centerOffset);
        }

        return offsets;
    }

    private static int closestToCenterIndex(List<Offset> offsets) {
        int bestIndex = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < offsets.size(); i++) {
            Offset o = offsets.get(i);
            double dist = o.dx() * o.dx() + o.dy() * o.dy();
            if (dist < bestDist) {
                bestDist = dist;
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
