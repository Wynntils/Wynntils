/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.event;

import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import com.wynntils.models.lootrun.type.TaskLocation;
import net.neoforged.bus.api.Event;

public class LootrunBeaconSelectedEvent extends Event {
    private final Beacon beacon;
    private final TaskLocation taskLocation;
    private final LootrunTaskType taskType;

    public LootrunBeaconSelectedEvent(Beacon beacon, TaskLocation taskLocation, LootrunTaskType taskType) {
        this.beacon = beacon;
        this.taskLocation = taskLocation;
        this.taskType = taskType;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public TaskLocation getTaskLocation() {
        return taskLocation;
    }

    public LootrunTaskType getTaskType() {
        return taskType;
    }
}
