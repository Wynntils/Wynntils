/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class SetEntityPassengersEvent extends Event {
    private final int vehicle;

    public SetEntityPassengersEvent(int vehicle) {
        this.vehicle = vehicle;
    }

    public int getVehicle() {
        return vehicle;
    }
}
