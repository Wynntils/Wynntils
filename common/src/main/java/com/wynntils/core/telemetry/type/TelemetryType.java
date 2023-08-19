/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.telemetry.type;

import com.wynntils.core.telemetry.datatype.LootrunTaskLocation;

/**
 * This enum represents the type of telemetry that is being collected.
 * Each data class has to implement {@link Comparable}.
 */
public enum TelemetryType {
    LOOTRUN_TASK_LOCATIONS(LootrunTaskLocation.class);

    private final Class<?> dataClass;

    TelemetryType(Class<?> dataClass) {
        this.dataClass = dataClass;
    }

    public Class<?> getDataClass() {
        return dataClass;
    }
}
