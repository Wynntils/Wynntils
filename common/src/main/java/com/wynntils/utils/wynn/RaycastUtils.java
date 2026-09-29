/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class RaycastUtils {
    private static final float RAYCAST_RANGE = 5f;

    public static Optional<Player> getHoveredPlayer() {
        LocalPlayer player = McUtils.player();

        Vec3 start = player.getEyePosition(1f);
        Vec3 look = player.getLookAngle();
        Vec3 direction = start.add(look.x * RAYCAST_RANGE, look.y * RAYCAST_RANGE, look.z * RAYCAST_RANGE);
        AABB boundingBox = player.getBoundingBox()
                .expandTowards(look.x * RAYCAST_RANGE, look.y * RAYCAST_RANGE, look.z * RAYCAST_RANGE)
                .expandTowards(1, 1, 1);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                McUtils.mc().level,
                player,
                start,
                direction,
                boundingBox,
                (e) -> e instanceof Player,
                ProjectileUtil.computeMargin(player));

        if (hitResult == null) return Optional.empty();

        return Optional.of((Player) hitResult.getEntity());
    }

    public static Optional<Display.TextDisplay> getTargetedLabel(
            List<Display.TextDisplay> candidates,
            double maxRange,
            double horizontalFovDegrees,
            double verticalFovDegrees) {
        LocalPlayer player = McUtils.mc().player;

        Vec3 camPos = player.getEyePosition();
        Vec3 camForward = player.getLookAngle();

        Display.TextDisplay best = null;
        double bestHorizontalAngle = Double.MAX_VALUE;
        double bestDistSq = Double.MAX_VALUE;

        double maxRangeSq = maxRange * maxRange;

        double halfHorizontalRad = Math.toRadians(horizontalFovDegrees * 0.5);
        double halfVerticalRad = Math.toRadians(verticalFovDegrees * 0.5);

        for (Display.TextDisplay display : candidates) {
            if (display.isRemoved()) continue;

            Vec3 to = display.position().subtract(camPos);
            double distSq = to.lengthSqr();
            if (distSq > maxRangeSq) continue;

            Vec3 toNorm = to.normalize();

            if (camForward.dot(toNorm) <= 0.0) continue;

            Vec3 camFlat = new Vec3(camForward.x, 0.0, camForward.z);
            Vec3 toFlat = new Vec3(toNorm.x, 0.0, toNorm.z);

            if (camFlat.lengthSqr() == 0 || toFlat.lengthSqr() == 0) continue;

            camFlat = camFlat.normalize();
            toFlat = toFlat.normalize();

            double horizontalDot = camFlat.dot(toFlat);
            double horizontalAngle = Math.acos(horizontalDot);

            if (horizontalAngle > halfHorizontalRad) continue;

            double camPitch = Math.asin(camForward.y);
            double labelPitch = Math.asin(toNorm.y);
            double verticalAngle = Math.abs(labelPitch - camPitch);

            if (verticalAngle > halfVerticalRad) continue;

            if (horizontalAngle < bestHorizontalAngle
                    || (horizontalAngle == bestHorizontalAngle && distSq < bestDistSq)) {
                bestHorizontalAngle = horizontalAngle;
                bestDistSq = distSq;
                best = display;
            }
        }

        return Optional.ofNullable(best);
    }

    public static Optional<BlockHitResult> getTargetedBlockHitResult(
            double maxDistance, boolean colliderOnly, boolean ignoreBarriers) {
        LocalPlayer player = McUtils.player();

        if (player.level() == null) return Optional.empty();

        Vec3 start = player.getEyePosition(1f);
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(maxDistance));

        ClipContext.Block blockType = colliderOnly ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE;

        if (!ignoreBarriers) {
            BlockHitResult hitResult =
                    player.level().clip(new ClipContext(start, end, blockType, ClipContext.Fluid.NONE, player));

            return hitResult.getType() == HitResult.Type.BLOCK ? Optional.of(hitResult) : Optional.empty();
        }

        Vec3 rayStart = start;

        while (rayStart.distanceToSqr(start) < start.distanceToSqr(end)) {
            BlockHitResult hitResult =
                    player.level().clip(new ClipContext(rayStart, end, blockType, ClipContext.Fluid.NONE, player));

            if (hitResult.getType() != HitResult.Type.BLOCK) return Optional.empty();

            if (!player.level().getBlockState(hitResult.getBlockPos()).is(Blocks.BARRIER)) {
                return Optional.of(hitResult);
            }

            rayStart = hitResult.getLocation().add(look.scale(0.0001));
        }

        return Optional.empty();
    }

    public static Optional<HitResult> getTargetedHitResult(
            double maxDistance, boolean colliderOnly, boolean ignoreBarriers) {
        LocalPlayer player = McUtils.player();

        if (player.level() == null) return Optional.empty();

        Vec3 start = player.getEyePosition(1f);
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(maxDistance));

        ClipContext.Block blockType = colliderOnly ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE;

        BlockHitResult blockHitResult;

        if (ignoreBarriers) {
            blockHitResult = getFirstNonBarrierBlockHit(player, start, end, look, blockType);
        } else {
            BlockHitResult hitResult =
                    player.level().clip(new ClipContext(start, end, blockType, ClipContext.Fluid.NONE, player));

            blockHitResult = hitResult.getType() == HitResult.Type.BLOCK ? hitResult : null;
        }

        double entityRange = blockHitResult == null ? maxDistance : start.distanceTo(blockHitResult.getLocation());

        EntityHitResult entityHitResult = getEntityHitResult(player, start, look, entityRange);

        if (entityHitResult != null) return Optional.of(entityHitResult);

        return Optional.ofNullable(blockHitResult);
    }

    private static BlockHitResult getFirstNonBarrierBlockHit(
            LocalPlayer player, Vec3 start, Vec3 end, Vec3 look, ClipContext.Block blockType) {
        Vec3 rayStart = start;

        while (rayStart.distanceToSqr(start) < start.distanceToSqr(end)) {
            BlockHitResult hitResult =
                    player.level().clip(new ClipContext(rayStart, end, blockType, ClipContext.Fluid.NONE, player));

            if (hitResult.getType() != HitResult.Type.BLOCK) return null;

            if (!player.level().getBlockState(hitResult.getBlockPos()).is(Blocks.BARRIER)) {
                return hitResult;
            }

            rayStart = hitResult.getLocation().add(look.scale(0.0001));
        }

        return null;
    }

    private static EntityHitResult getEntityHitResult(LocalPlayer player, Vec3 start, Vec3 look, double maxDistance) {
        Vec3 end = start.add(look.scale(maxDistance));

        AABB boundingBox =
                player.getBoundingBox().expandTowards(look.scale(maxDistance)).inflate(1.0d);

        return ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                start,
                end,
                boundingBox,
                entity -> entity.isPickable() && !entity.isSpectator(),
                ProjectileUtil.computeMargin(player));
    }

    public static Optional<BlockPos> getTargetedBlockPosition(double maxDistance, boolean colliderOnly) {
        return getTargetedBlockHitResult(maxDistance, colliderOnly, false).map(BlockHitResult::getBlockPos);
    }
}
