/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class PosUtils {
    public static BlockPos newBlockPos(double x, double y, double z) {
        return BlockPos.containing(x, y, z);
    }

    public static BlockPos newBlockPos(Position position) {
        return BlockPos.containing(position);
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
        return first.x() == second.x() && first.z() == second.z() && Math.abs(first.y() - second.y()) < 1.5;
    }

    public static boolean equalsIgnoringY(Position pos1, Position pos2) {
        return pos1.x() == pos2.x() && pos1.z() == pos2.z();
    }

    public static boolean closerThanIgnoringY(Position pos1, Position pos2, double distance) {
        double xD = pos1.x() - pos2.x();
        double zD = pos1.z() - pos2.z();
        return xD * xD + zD * zD < distance * distance;
    }
}
