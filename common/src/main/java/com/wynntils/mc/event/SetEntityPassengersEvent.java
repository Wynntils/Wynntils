/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;

public class SetEntityPassengersEvent extends BaseEvent implements OperationCancelable {
    private final int vehicle;

    public SetEntityPassengersEvent(int vehicle) {
        this.vehicle = vehicle;
    }

    public int getVehicle() {
        return vehicle;
    }
}
