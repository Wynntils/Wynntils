/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.telemetry.type;

import com.wynntils.core.telemetry.datatype.LootrunTaskLocation;

/**
 * This enum represents the type of crowd sources data that is being collected.
 * Each data class has to implement {@link Comparable}.
 */
public enum CrowdSourceDataType {
    LOOTRUN_TASK_LOCATIONS(LootrunTaskLocation.class);

    private final Class<?> dataClass;

    CrowdSourceDataType(Class<?> dataClass) {
        this.dataClass = dataClass;
    }

    public Class<?> getDataClass() {
        return dataClass;
    }
}
