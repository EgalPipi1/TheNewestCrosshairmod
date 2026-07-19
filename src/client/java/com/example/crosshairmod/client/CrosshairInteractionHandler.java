package com.example.crosshairmod.client;

import com.example.crosshairmod.config.CrosshairConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

/**
 * Each extra crosshair beyond the vanilla center one (index 0) gets its own
 * ray cast into the world using the same screen-space offset the HUD draws
 * it at. On a fresh left-click (attack/mine key), every crosshair that has
 * a valid target within reach gets an attack (entity) or an instant-break
 * attempt (block) fired at it, just like clicking there directly would.
 *
 * NOTE: this fires once per key press rather than continuously while held,
 * and block breaking is only reliable for creative-mode instant break or
 * low-hardness blocks in survival, since vanilla's block-breaking progress
 * tracking is built around a single target. Using this to hit multiple
 * entities per click on a multiplayer server is exactly the kind of thing
 * anti-cheat systems and server rules flag as unfair -- treat this as a
 * singleplayer/creative tool.
 */
public final class CrosshairInteractionHandler {

    private static final double ENTITY_HITBOX_MARGIN = 0.3;

    private boolean wasAttackKeyDown = false;

    public void onEndTick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) {
            wasAttackKeyDown = false;
            return;
        }
        if (client.currentScreen != null) {
            wasAttackKeyDown = false;
            return;
        }

        boolean attackDown = client.options.attackKey.isPressed();
        boolean justPressed = attackDown && !wasAttackKeyDown;
        wasAttackKeyDown = attackDown;

        if (!justPressed) return;
        if (CrosshairConfig.count <= 1) return; // nothing extra to do beyond vanilla's own crosshair

        fireExtraCrosshairs(client);
    }

    private void fireExtraCrosshairs(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        Camera camera = client.gameRenderer.getCamera();

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        List<CrosshairMath.Offset> offsets =
                CrosshairMath.computeOffsets(CrosshairConfig.count, CrosshairConfig.range, CrosshairConfig.density);

        double reach = client.interactionManager.getReachDistance();

        double vFovDegrees = client.options.getFov().getValue();
        double vFovRad = Math.toRadians(vFovDegrees);
        double vHalfExtent = Math.tan(vFovRad / 2.0);
        double aspect = (double) screenWidth / (double) screenHeight;
        double hHalfExtent = vHalfExtent * aspect;

        // Skip index 0: that's the vanilla center crosshair, already handled by the game itself.
        for (int i = 1; i < offsets.size(); i++) {
            CrosshairMath.Offset offset = offsets.get(i);

            double ndcX = offset.dx() / (screenWidth / 2.0);
            double ndcY = offset.dy() / (screenHeight / 2.0);

            Vector3f localDir = new Vector3f(
                    (float) (ndcX * hHalfExtent),
                    (float) -(ndcY * vHalfExtent),
                    -1f
            ).normalize();

            Quaternionf rotation = new Quaternionf(camera.getRotation());
            Vector3f worldDir = rotation.transform(localDir);

            Vec3d start = camera.getPos();
            Vec3d direction = new Vec3d(worldDir.x, worldDir.y, worldDir.z);

            HitResult hit = raycast(client, player, start, direction, reach);
            if (hit == null) continue;

            if (hit instanceof EntityHitResult entityHit) {
                attackEntity(client, player, entityHit.getEntity());
            } else if (hit instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
                mineBlock(client, blockHit);
            }
        }
    }

    private HitResult raycast(MinecraftClient client, ClientPlayerEntity player,
                               Vec3d start, Vec3d direction, double reach) {
        Vec3d end = start.add(direction.multiply(reach));

        // Block raycast first, to know how far we can legitimately reach before hitting terrain.
        RaycastContext ctx = new RaycastContext(
                start, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        );
        BlockHitResult blockHit = client.world.raycast(ctx);
        double blockDistance = blockHit.getType() == HitResult.Type.MISS
                ? reach
                : blockHit.getPos().distanceTo(start);

        Entity entityHit = findClosestEntity(client, player, start, direction, Math.min(reach, blockDistance));
        if (entityHit != null) {
            return new EntityHitResult(entityHit);
        }
        if (blockHit.getType() != HitResult.Type.MISS) {
            return blockHit;
        }
        return null;
    }

    private Entity findClosestEntity(MinecraftClient client, PlayerEntity player,
                                      Vec3d start, Vec3d direction, double maxDistance) {
        Vec3d end = start.add(direction.multiply(maxDistance));
        Box searchBox = player.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(1.0);

        Entity closest = null;
        double closestDistance = maxDistance;

        for (Entity entity : client.world.getOtherEntities(player, searchBox, e -> e.isAlive() && e.canHit())) {
            Box box = entity.getBoundingBox().expand(ENTITY_HITBOX_MARGIN);
            Optional<Vec3d> intersection = box.raycast(start, end);
            if (intersection.isPresent()) {
                double distance = start.distanceTo(intersection.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = entity;
                }
            }
        }
        return closest;
    }

    private void attackEntity(MinecraftClient client, ClientPlayerEntity player, Entity target) {
        client.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
    }

    private void mineBlock(MinecraftClient client, BlockHitResult blockHit) {
        // attackBlock starts/continues breaking; in creative this instantly breaks the block.
        // In survival, a single call only registers a hit -- reliable mainly for low-hardness
        // blocks or creative mode, since we don't track multi-position breaking progress.
        client.interactionManager.attackBlock(blockHit.getBlockPos(), blockHit.getSide());
        client.player.swingHand(Hand.MAIN_HAND);
    }
}
