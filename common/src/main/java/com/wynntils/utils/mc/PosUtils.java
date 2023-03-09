/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class PosUtils {
    public static BlockPos newBlockPos(double x, double y, double z) {
        return new BlockPos((int) x, (int) y, (int) z);
    }

    public static BlockPos newBlockPos(Vec3 vec3) {
        return new BlockPos((int) vec3.x, (int) vec3.y, (int) vec3.z);
    }
}
