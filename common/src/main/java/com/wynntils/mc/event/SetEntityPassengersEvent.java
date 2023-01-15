/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class SetEntityPassengersEvent extends WynntilsEvent {
    private final int vehicle;

    public SetEntityPassengersEvent(int vehicle) {
        this.vehicle = vehicle;
    }

    public int getVehicle() {
        return vehicle;
    }
}
