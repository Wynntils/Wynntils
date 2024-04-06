/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.google.common.collect.ComparisonChain;
import com.wynntils.utils.mc.type.Location;

public record TaskLocation(String name, Location location, LootrunTaskType taskType)
        implements Comparable<TaskLocation> {
    @Override
    public int compareTo(TaskLocation taskLocation) {
        return ComparisonChain.start()
                .compare(location, taskLocation.location)
                .compare(taskType, taskLocation.taskType)
                .result();
    }
}
