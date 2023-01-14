package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;

public class GearStatHolder implements GearStat{
    private final String key;
    private final String displayName;
    private final String apiName;
    private final String loreName;
    private final GearStatUnit unit;

    GearStatHolder(String key, String displayName, String apiName, String loreName, GearStatUnit unit) {
        this.key = key;
        this.displayName = displayName;
        this.apiName = apiName;
        this.loreName = loreName;
        this.unit = unit;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getApiName() {
        return apiName;
    }

    @Override
    public String getLoreName() {
        return loreName;
    }

    @Override
    public GearStatUnit getUnit() {
        return unit;
    }
}
