/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.event;

import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.Event;

public class LootChestOpenedEvent extends Event {
    private final BlockPos blockPos;

    public LootChestOpenedEvent(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
