/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.lootrun.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class RecordingInformation {
    private Vec3 lastLocation;
    private BlockPos lastChest;
    private boolean dirty;

    public Vec3 getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Vec3 lastLocation) {
        this.lastLocation = lastLocation;
    }

    public BlockPos getLastChest() {
        return lastChest;
    }

    public void setLastChest(BlockPos lastChest) {
        this.lastChest = lastChest;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
