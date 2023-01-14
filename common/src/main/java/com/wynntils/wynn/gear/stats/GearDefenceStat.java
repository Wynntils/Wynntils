/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

public enum GearDefenceStat implements GearStat {
    // FIXME: check discrepancy between lore and api
    DEFENCE_AIR("Air Defence", "%", "AIRDEFENSE", "bonusAirDefense"),
    DEFENCE_EARTH("Earth Defence", "%", "EARTHDEFENSE", "bonusEarthDefense"),
    DEFENCE_FIRE("Fire Defence", "%", "FIREDEFENSE", "bonusFireDefense"),
    DEFENCE_THUNDER("Thunder Defence", "%", "THUNDERDEFENSE", "bonusThunderDefense"),
    DEFENCE_WATER("Water Defence", "%", "WATERDEFENSE", "bonusWaterDefense");

    private final String displayName;
    private final String unit;
    private final String loreName;
    private final String apiName;

    GearDefenceStat(String displayName, String unit, String loreName, String apiName) {
        this.displayName = displayName;
        this.unit = unit;
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
    public String getUnit() {
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
