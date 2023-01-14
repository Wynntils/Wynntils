/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;

public enum GearDefenceStat implements GearStat {
    // Lore confirmed!
    DEFENCE_AIR("Air Defence", "AIRDEFENSE", "bonusAirDefense"),
    DEFENCE_EARTH("Earth Defence", "EARTHDEFENSE", "bonusEarthDefense"),
    DEFENCE_FIRE("Fire Defence", "FIREDEFENSE", "bonusFireDefense"),
    DEFENCE_THUNDER("Thunder Defence", "THUNDERDEFENSE", "bonusThunderDefense"),
    DEFENCE_WATER("Water Defence", "WATERDEFENSE", "bonusWaterDefense");

    private final String displayName;
    private final GearStatUnit unit;
    private final String loreName;
    private final String apiName;

    GearDefenceStat(String displayName, String loreName, String apiName) {
        this.displayName = displayName;
        this.unit = GearStatUnit.PERCENT;
        this.loreName = loreName;
        this.apiName = apiName;
    }

    @Override
    public String getKey() {
        return this.name();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public GearStatUnit getUnit() {
        return unit;
    }

    @Override
    public String getLoreName() {
        return loreName;
    }

    @Override
    public String getApiName() {
        return apiName;
    }
}
