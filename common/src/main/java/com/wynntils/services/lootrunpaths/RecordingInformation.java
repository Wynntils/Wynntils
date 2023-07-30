/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.lootrunpaths;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

class RecordingInformation {
    private Vec3 lastLocation;
    private BlockPos lastChest;
    private boolean dirty;

    protected Vec3 getLastLocation() {
        return lastLocation;
    }

    protected void setLastLocation(Vec3 lastLocation) {
        this.lastLocation = lastLocation;
    }

    protected BlockPos getLastChest() {
        return lastChest;
    }

    protected void setLastChest(BlockPos lastChest) {
        this.lastChest = lastChest;
    }

    protected boolean isDirty() {
        return dirty;
    }

    protected void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
