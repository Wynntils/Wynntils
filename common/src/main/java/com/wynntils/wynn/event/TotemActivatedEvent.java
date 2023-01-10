/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import com.wynntils.mc.objects.Location;

public class TotemActivatedEvent extends TotemEvent {
    private final int time;
    private final Location location;

    public TotemActivatedEvent(int totemNumber, int time, Location location) {
        super(totemNumber);
        this.time = time;
        this.location = location;
    }

    public int getTime() {
        return time;
    }

    public Location getLocation() {
        return location;
    }
}
