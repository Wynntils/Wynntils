/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.telemetry.type;

import com.wynntils.core.telemetry.datatype.LootrunTaskLocation;

/**
 * This enum represents the type of crowd sourced data that is being collected.
 */
public enum CrowdSourcedDataType {
    LOOTRUN_TASK_LOCATIONS(LootrunTaskLocation.class);

    private final Class<? extends Comparable<?>> dataClass;

    CrowdSourcedDataType(Class<? extends Comparable<?>> dataClass) {
        this.dataClass = dataClass;
    }

    public Class<? extends Comparable<?>> getDataClass() {
        return dataClass;
    }
}
