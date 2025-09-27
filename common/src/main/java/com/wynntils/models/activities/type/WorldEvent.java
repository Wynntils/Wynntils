/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import com.wynntils.utils.type.Time;

public class WorldEvent {
    private final String name;

    private Time startTime;

    public WorldEvent(String name, Time startTime) {
        this.name = name;
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getStartTime() {
        return startTime;
    }
}
