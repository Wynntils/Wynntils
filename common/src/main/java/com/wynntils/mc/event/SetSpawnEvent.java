/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class SetSpawnEvent extends WynntilsEvent {
    private final BlockPos spawnPos;

    public SetSpawnEvent(BlockPos spawnPos) {
        this.spawnPos = spawnPos;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }
}
