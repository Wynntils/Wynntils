/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.wynntils.utils.mc.PosUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum PathBlockValidness {
    VALID,
    HAS_BARRIER,
    INVALID;

    public static PathBlockValidness checkPathBlockValidness(Level level, ColoredPosition point) {
        boolean solid = false;
        for (BlockPos block : getBlocksForPoint(point)) {
            BlockState state = level.getBlockState(block);
            if (state.is(Blocks.BARRIER)) return HAS_BARRIER;

            if (!state.getCollisionShape(level, block).isEmpty()) {
                solid = true;
            }
        }

        return solid ? VALID : INVALID;
    }

    private static Iterable<BlockPos> getBlocksForPoint(ColoredPosition loc) {
        BlockPos minPos = PosUtils.newBlockPos(
                loc.position().x() - 0.3D,
                loc.position().y() - 1D,
                loc.position().z() - 0.3D);
        BlockPos maxPos = PosUtils.newBlockPos(
                loc.position().x() + 0.3D,
                loc.position().y() - 1D,
                loc.position().z() + 0.3D);

        return BlockPos.betweenClosed(minPos, maxPos);
    }
}
