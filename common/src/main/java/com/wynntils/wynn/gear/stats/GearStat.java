/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;

public class GearStat {
    private final String key;
    private final String displayName;
    private final String apiName;
    private final String loreName;
    private final GearStatUnit unit;

    GearStat(String key, String displayName, String apiName, String loreName, GearStatUnit unit) {
        this.key = key;
        this.displayName = displayName;
        this.apiName = apiName;
        this.loreName = loreName;
        this.unit = unit;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }

    public String getLoreName() {
        return loreName;
    }

    public GearStatUnit getUnit() {
        return unit;
    }
}
