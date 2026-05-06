/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class DestroyBlockEvent extends Event implements ICancellableEvent {
    private final BlockPos pos;
    private final Direction direction;
    private final boolean continuing;

    public DestroyBlockEvent(BlockPos pos, Direction direction, boolean continuing) {
        this.pos = pos;
        this.direction = direction;
        this.continuing = continuing;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isContinuing() {
        return continuing;
    }
}
