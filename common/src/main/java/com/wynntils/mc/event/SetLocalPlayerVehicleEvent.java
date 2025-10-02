/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.world.entity.Entity;

public class SetLocalPlayerVehicleEvent extends BaseEvent {
    private final Entity vehicle;

    public SetLocalPlayerVehicleEvent(Entity vehicle) {
        this.vehicle = vehicle;
    }

    public Entity getVehicle() {
        return this.vehicle;
    }
}
