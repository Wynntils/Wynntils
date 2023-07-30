/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.lootrunpaths.type;

import com.wynntils.utils.mc.PosUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum BlockValidness {
    VALID,
    HAS_BARRIER,
    INVALID;

    public static BlockValidness checkBlockValidness(Level level, ColoredPosition point) {
        BlockValidness state = INVALID;
        Iterable<BlockPos> blocks = getBlocksForPoint(point);

        for (BlockPos blockInArea : blocks) {
            BlockState blockStateInArea = level.getBlockState(blockInArea);
            if (blockStateInArea.is(Blocks.BARRIER)) {
                state = HAS_BARRIER;
            } else if (blockStateInArea.getCollisionShape(level, blockInArea) != null) {
                state = VALID;
                return state;
            }
        }

        return state;
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
