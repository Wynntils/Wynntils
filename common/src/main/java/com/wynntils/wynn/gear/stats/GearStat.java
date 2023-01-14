/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

public interface GearStat {
    String getKey();

    String getDisplayName();

    String getUnit();

    String getLoreName();

    String getApiName();
}
