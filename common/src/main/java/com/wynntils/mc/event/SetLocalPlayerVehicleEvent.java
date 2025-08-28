/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

public class SetLocalPlayerVehicleEvent extends Event {
    private final Entity vehicle;

    public SetLocalPlayerVehicleEvent(Entity vehicle) {
        this.vehicle = vehicle;
    }

    public Entity getVehicle() {
        return this.vehicle;
    }
}
