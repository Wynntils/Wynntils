/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.google.common.collect.ComparisonChain;
import com.wynntils.utils.mc.type.Location;

// This location has to be a Location because Position doesn't have proper mapping, so GSON can't serialize it.
public record TaskLocation(Location location, LootrunTaskType taskType) implements Comparable<TaskLocation> {
    @Override
    public int compareTo(TaskLocation taskLocation) {
        return ComparisonChain.start()
                .compare(location.x(), taskLocation.location.x())
                .compare(location.y(), taskLocation.location.y())
                .compare(location.z(), taskLocation.location.z())
                .compare(taskType, taskLocation.taskType)
                .result();
    }
}
