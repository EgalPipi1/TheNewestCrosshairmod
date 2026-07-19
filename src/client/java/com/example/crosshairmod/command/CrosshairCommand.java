package com.example.crosshairmod.command;

import com.example.crosshairmod.config.CrosshairConfig;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

/**
 * Registers the /crosshair command tree.
 * This is a CLIENT command (fabric-api client command API) so it works
 * entirely locally, without needing the mod installed on a server.
 */
public final class CrosshairCommand {

    private CrosshairCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("crosshair")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(Text.literal(
                                    "§eUsage: §f/crosshair <count|range|density|size|thickness|color|reset|info> [value]"));
                            return 1;
                        })
                        .then(ClientCommandManager.literal("count")
                                .then(ClientCommandManager.argument("value",
                                                IntegerArgumentType.integer(CrosshairConfig.MIN_COUNT, CrosshairConfig.MAX_COUNT))
                                        .executes(ctx -> {
                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                            CrosshairConfig.count = value;
                                            CrosshairConfig.save();
                                            ctx.getSource().sendFeedback(Text.literal(
                                                    "§aCrosshair count set to §f" + value));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("range")
                                .then(ClientCommandManager.argument("value",
                                                FloatArgumentType.floatArg(CrosshairConfig.MIN_RANGE, CrosshairConfig.MAX_RANGE))
                                        .executes(ctx -> {
                                            float value = FloatArgumentType.getFloat(ctx, "value");
                                            CrosshairConfig.range = value;
                                            CrosshairConfig.save();
                                            ctx.getSource().sendFeedback(Text.literal(
                                                    "§aCrosshair range set to §f" + value + " §7px"));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("density")
                                .then(ClientCommandManager.argument("value",
                                                FloatArgumentType.floatArg(CrosshairConfig.MIN_DENSITY, CrosshairConfig.MAX_DENSITY))
                                        .executes(ctx -> {
                                            float value = FloatArgumentType.getFloat(ctx, "value");
                                            CrosshairConfig.density = value;
                                            CrosshairConfig.save();
                                            ctx.getSource().sendFeedback(Text.literal(
                                                    "§aCrosshair density set to §f" + value +
                                                    " §7(>1 = clustered center, <1 = spread to edge)"));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("size")
                                .then(ClientCommandManager.argument("value",
                                                IntegerArgumentType.integer(CrosshairConfig.MIN_SIZE, CrosshairConfig.MAX_SIZE))
                                        .executes(ctx -> {
                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                            CrosshairConfig.size = value;
                                            CrosshairConfig.save();
                                            ctx.getSource().sendFeedback(Text.literal(
                                                    "§aCrosshair arm size set to §f" + value + " §7px"));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("thickness")
                                .then(ClientCommandManager.argument("value",
                                                IntegerArgumentType.integer(CrosshairConfig.MIN_THICKNESS, CrosshairConfig.MAX_THICKNESS))
                                        .executes(ctx -> {
                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                            CrosshairConfig.thickness = value;
                                            CrosshairConfig.save();
                                            ctx.getSource().sendFeedback(Text.literal(
                                                    "§aCrosshair thickness set to §f" + value + " §7px"));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("color")
                                .then(ClientCommandManager.argument("hex", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String hex = StringArgumentType.getString(ctx, "hex").replace("#", "");
                                            try {
                                                long parsed = Long.parseLong(hex, 16);
                                                int value = (int) parsed;
                                                if (hex.length() <= 6) {
                                                    value |= 0xFF000000; // default full opacity if no alpha given
                                                }
                                                CrosshairConfig.color = value;
                                                CrosshairConfig.save();
                                                ctx.getSource().sendFeedback(Text.literal(
                                                        "§aCrosshair color set to §f#" + hex));
                                            } catch (NumberFormatException e) {
                                                ctx.getSource().sendFeedback(Text.literal(
                                                        "§cInvalid hex color. Use e.g. FFFFFF or AARRGGBB like FF00FF00"));
                                            }
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("reset")
                                .executes(ctx -> {
                                    CrosshairConfig.reset();
                                    CrosshairConfig.save();
                                    ctx.getSource().sendFeedback(Text.literal("§aCrosshair settings reset to defaults"));
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("info")
                                .executes(ctx -> {
                                    ctx.getSource().sendFeedback(Text.literal(String.format(
                                            "§ecount=§f%d §e range=§f%.1f §e density=§f%.2f §e size=§f%d §e thickness=§f%d",
                                            CrosshairConfig.count, CrosshairConfig.range, CrosshairConfig.density,
                                            CrosshairConfig.size, CrosshairConfig.thickness)));
                                    return 1;
                                }))
                )
        );
    }
}
