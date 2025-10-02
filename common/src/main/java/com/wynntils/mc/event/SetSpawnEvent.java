/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;
import net.minecraft.core.BlockPos;

public class SetSpawnEvent extends BaseEvent implements OperationCancelable {
    private final BlockPos spawnPos;

    public SetSpawnEvent(BlockPos spawnPos) {
        this.spawnPos = spawnPos;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }
}
