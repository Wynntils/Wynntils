/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class SetSpawnEvent extends Event {
    private final BlockPos spawnPos;

    public SetSpawnEvent(BlockPos spawnPos) {
        this.spawnPos = spawnPos;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }
}
