package com.example.crosshairmod.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Holds all adjustable crosshair settings and persists them between sessions.
 * All values are clamped so commands can never push the renderer into an
 * invalid or crash-prone state.
 */
public class CrosshairConfig {

    // ---- Live settings (defaults) ----
    public static int count = 8;        // how many crosshairs to draw
    public static float range = 80f;    // radius in pixels the outer grid edge sits at
    public static float density = 1.0f; // grid spacing: >1 tighter, <1 looser
    public static int size = 8;         // crosshair arm length in pixels (vanilla-ish thin cross)
    public static int thickness = 1;    // crosshair arm thickness in pixels
    public static int color = 0xFFFFFFFF; // ARGB tint (white = vanilla color)

    // ---- Bounds ----
    public static final int MIN_COUNT = 0;
    public static final int MAX_COUNT = 2000;
    public static final float MIN_RANGE = 0f;
    public static final float MAX_RANGE = 2000f;
    public static final float MIN_DENSITY = 0.05f;
    public static final float MAX_DENSITY = 10f;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 64;
    public static final int MIN_THICKNESS = 1;
    public static final int MAX_THICKNESS = 16;

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("crosshairmod.properties");

    public static void reset() {
        count = 8;
        range = 80f;
        density = 1.0f;
        size = 8;
        thickness = 1;
        color = 0xFFFFFFFF;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            props.load(in);
            count = clampInt(parseIntSafe(props.getProperty("count"), count), MIN_COUNT, MAX_COUNT);
            range = clampFloat(parseFloatSafe(props.getProperty("range"), range), MIN_RANGE, MAX_RANGE);
            density = clampFloat(parseFloatSafe(props.getProperty("density"), density), MIN_DENSITY, MAX_DENSITY);
            size = clampInt(parseIntSafe(props.getProperty("size"), size), MIN_SIZE, MAX_SIZE);
            thickness = clampInt(parseIntSafe(props.getProperty("thickness"), thickness), MIN_THICKNESS, MAX_THICKNESS);
            color = (int) parseLongSafe(props.getProperty("color"), color & 0xFFFFFFFFL);
        } catch (IOException e) {
            // Fall back to defaults silently; config will be rewritten on next save.
        }
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("count", String.valueOf(count));
        props.setProperty("range", String.valueOf(range));
        props.setProperty("density", String.valueOf(density));
        props.setProperty("size", String.valueOf(size));
        props.setProperty("thickness", String.valueOf(thickness));
        props.setProperty("color", Long.toHexString(color & 0xFFFFFFFFL));
        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            props.store(out, "Multi Crosshair mod settings");
        } catch (IOException ignored) {
            // Non-fatal: settings just won't persist to disk this run.
        }
    }

    public static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int parseIntSafe(String s, int fallback) {
        if (s == null) return fallback;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static float parseFloatSafe(String s, float fallback) {
        if (s == null) return fallback;
        try {
            return Float.parseFloat(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long parseLongSafe(String s, long fallback) {
        if (s == null) return fallback;
        try {
            return Long.parseLong(s.trim(), 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
