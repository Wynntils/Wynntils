/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class PosUtils {
    public static BlockPos newBlockPos(double x, double y, double z) {
        return new BlockPos((int) x, (int) y, (int) z);
    }

    public static BlockPos newBlockPos(Position position) {
        return new BlockPos((int) position.x(), (int) position.y(), (int) position.z());
    }

    public static Position newPosition(Entity entity) {
        return new PositionImpl(entity.getX(), entity.getY(), entity.getZ());
    }

    public static Vec3 toVec3(Position position) {
        return new Vec3(position.x(), position.y(), position.z());
    }

    /** Return true if the two positions is "roughly" the same, i.e. they only differ slightly
     * in the y position.
     */
    public static boolean isSame(Position first, Position second) {
        return second.x() == first.x() && second.z() == first.z() && Math.abs(second.y() - first.y()) < 1.5;
    }
}
